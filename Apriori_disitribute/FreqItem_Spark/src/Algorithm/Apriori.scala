package Algorithm


import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.immutable.SortedSet
import org.apache.spark.SparkContext._

import scala.collection.mutable.ArrayBuffer

  
object Apriori {
  def main (args: Array[String]){

    val conf = new SparkConf()
    conf.setAppName("Apriori")
    //conf.setMaster("local")

    val sc = new SparkContext(conf)

    val out_dir = "xrli/FreqItem/out/"
    val m_ratio = 0.9
    val lines = sc.textFile("xrli/FreqItem/chess.data")
    
    
    val startTime = System.currentTimeMillis()
    val db = lines.map(_.split(" ").map(each =>SortedSet(each.toInt)).reduce(_ ++ _)).cache()   // SortedSet(each.toInt)表示先把单个元素转SortedSet， 然后用 reduce_ ++ _ 合并为大的SortedSet
    
    val minSupport = ( db.count() * m_ratio ).toInt

	 //1频繁集项
    var Lk = lines.flatMap(_.split(" ")).map(word => (SortedSet(word.toInt), 1)).reduceByKey(_ + _).filter(_._2 >= minSupport).cache()   //收集K_1项集

    var Lk_last = Lk.map( kv => kv._1)
    var sign = 0
    
    while(Lk.count() != 0){
      sign = sign + 1
      Lk.saveAsTextFile(out_dir + sign.toString)
      Lk_last = Lk.map( kv => kv._1)
      val L_k = Lk_last.collect()
      val Candidate_k = AprioriGen(L_k)
      val Breadcast_Ck = sc.broadcast(Candidate_k)
      Lk = db.flatMap{   //对新生成的候选集进行频率统计（分布式统计），筛选出大于支持度的item
        kv =>
          val temp = ArrayBuffer[(SortedSet[Int] , Int)]()
          Breadcast_Ck.value.foreach{
            k =>
              if(k.subsetOf(kv))
              {
                val s = (k,1)
                temp += s
              }
          }
          temp
      }.reduceByKey(_ + _).filter(_._2 >= minSupport).cache()
      Breadcast_Ck.unpersist()

    }
    if(sign == 0)
      println("minSupport is larger than 1-itemset")
    else
      Lk_last.collect().foreach(println(_))

    println(System.currentTimeMillis() - startTime)
  }

  // k 频繁项集 to k + 1 候选集   //这一段不需要分布式，因为数据量不大。生成 k + 1 候选集以后，broadcast,统计频率的时候要分布式
  def AprioriGen(L_k: Array[SortedSet[Int]]): Array[(SortedSet[Int])] = {

    val Ck = ArrayBuffer[SortedSet[Int]]()
    L_k.foreach{
      l1 =>   //因为是SortedSet，所以只有L1 L2 的前K-1项一样，这样L1 和L2就能尝试连接为K+1候选集c。  ABC, ABD => ABCD
        L_k.foreach{ 
          l2 =>    //你有可能会问 为什么不考虑 ABC, BCD => ABCD  ？ 详见图片75行说明。  如果ABC BCD都是频繁集，那么ABD也一定是频繁集，所以结果跟ABC, ABD => ABCD 是一样的。  但是如果是考虑顺序的话，就不一定了。
            if( l1.init == l2.init && l1.last < l2.last){   //init是除了最后一个元素之外的所有元素
              val c = l1.init + l1.last + l2.last
              if(!has_infrequent_subset(c,L_k)){   //判断刚生成的K+1候选集c 
                Ck += c
              }
            }
        }
    }
    Ck.toArray
  }
  
   //判断新生成的k+1候选集是否包含非频繁k项集
  //一个项集，如果有至少一个非空子集是非频繁的，那么这个项集一定是非频繁的。需要预先去掉，就不用再进行下一步的频率统计了

  def has_infrequent_subset(candidate : SortedSet[Int] , L_k : Array[SortedSet[Int]]): Boolean ={
	candidate.subsets(candidate.size -1).foreach{
      s =>
        if(!L_k.contains(s))
          return true         //只要发现一个非频繁K子集，就跳出
    }
    return false
  }
}




//5频繁项集输出示例如下：
//(TreeSet(29, 52, 58, 60, 66),3001)
//(TreeSet(7, 40, 52, 58, 62),2914)
//(TreeSet(36, 40, 48, 52, 58),2950)
//(TreeSet(34, 40, 52, 58, 62),2892)