package flow


import org.apache.spark.{SparkContext, SparkConf}

import org.apache.spark.SparkContext._
import scala.collection.immutable.SortedSet
 
import scala.collection.mutable.ArrayBuffer
 
object freqFlow_test {
  def main (args: Array[String]){

    val conf = new SparkConf()
    conf.setAppName("AprioriSeq")
    //conf.setMaster("local")

    val sc = new SparkContext(conf)

    val out_dir = "xrli/FreqItem/APPout/"
    val minSupport = 200
    
    val lines = sc.textFile("xrli/FreqItem/sorted_formated.csv")
    
    val startTime = System.currentTimeMillis()
	
	var arrdata = lines.map(line=>{
	   val arr = line.split(",")
	   (arr(0),(arr(1),arr(2).toLong))
	  }
	)
	
	val createCombiner = ( (v : (String,Long)) => List[(String,Long)](v) )
	
	val mergeValue =  (c : List[(String,Long)], v : (String,Long)) => 
		{
			var templist = v :: c
			var	sortedlist = templist.sortBy(f=>(f._2))  //根据时间排序
			sortedlist   //排序之后只保留item
		}

	val mergeCombiners =  (c1: List[(String,Long)], c2: List[(String,Long)]) => 
		{
			var templist = c1 ::: c2
			var	sortedlist = templist.sortBy(f=>(f._2))
			sortedlist
		}
	
	val seqRDD = arrdata.combineByKey(  
		createCombiner,
		mergeValue,
		mergeCombiners
	).map(temp=>temp._2.map(p=>p._1))   //把user去掉，并且list 的每一个item把时间去掉，     现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	 
//   seqRDD.collect().foreach{ println } 
	
    val db = seqRDD.cache()   //split with " "
 
	//1频繁集项
    var Lk = lines.map(_.split(",")).map(x=>(List(x(1)),1)).reduceByKey(_ + _).filter(_._2 >= minSupport).cache()   //收集K_1项集
 
    println("db count:" + db.count() + " minSupport:" + minSupport + " Lk count: " + Lk.count())
    
    var Lk_last = Lk.map( kv => kv._1)
    var sign = 0
    while(Lk.count() != 0){
      sign = sign + 1
      Lk.saveAsTextFile(out_dir + sign.toString)
      println("Round: " + sign)
      Lk.collect().foreach{ println} 
      
      Lk_last = Lk.map( kv => kv._1)
      val L_k = Lk_last.collect()
      val Candidate_k = AprioriSeqGen(L_k)
      val Breadcast_Ck = sc.broadcast(Candidate_k)
      Lk = db.flatMap{   //对新生成的候选集进行频率统计（分布式统计），筛选出大于支持度的item
        kv =>
          val temp = ArrayBuffer[(List[String] , Int)]()
          Breadcast_Ck.value.foreach{
            k =>
              if(kv.containsSlice(k))   //判断每一个kv是否包含频繁项集k
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
    else{
      println("Lk_last: ") 
      Lk_last.collect().foreach(println(_))
    }

    println("Spend time: " + (System.currentTimeMillis() - startTime))
  }

  // k 频繁项集 to k + 1 候选集   //这一段不需要分布式，因为数据量不大。生成 k + 1 候选集以后，broadcast,统计频率的时候要分布式
  //因为是List，并且要按顺序拼接，  ABC, ABD => ABCD  AB是相似的，可能拼接成 ABCD 或者ABDC （对有顺序的来说这是不一样的）       
  //另一种 ABC, BCD => ABCD  中间BC是相似的，可能拼接成 ABCD     总共应该就这三种可能
  def AprioriSeqGen(L_k: Array[List[String]]): Array[(List[String])] = {
	  val len_Lk = L_k.length
    val Ck = ArrayBuffer[List[String]]()
    
    if(L_k(0).size>1){ //判断是否是>2的频繁集项
      for(i<-0 to len_Lk-1){
        for(j<-0 to len_Lk-1){    //如果不考虑相互顺序， for(j<-i+1 to len_Lk-1) 就行了，但是这里要考虑相互顺序的，所以要全部遍历
  		    val L1_ini = L_k(i).init   //   L_k(i)[:len_Lk-1]
  	      val L1_tail = L_k(i).tail  //   L_k(i)[1:]
  		    val L2_ini = L_k(j).init
   
  		    if(L1_ini == L2_ini){
              val c1 = L1_ini.:+(L_k(i).last).:+(L_k(j).last)    //  :+表示添加元素到尾部     https://www.cnblogs.com/weilunhui/p/5658860.html
              if(!has_infrequent_subset(c1,L_k)){  
                Ck += c1
              }
  			  
  			      val c2 = L1_ini.:+(L_k(j).last).:+(L_k(i).last)
                if(!has_infrequent_subset(c2,L_k)){  
                  Ck += c2
                }
              }
  		  
  		    if(L1_tail == L2_ini){
                val c = L1_tail.+:( L_k(i).head).:+(L_k(j).last)  //  +: 表示添加元素到头部
                if(!has_infrequent_subset(c,L_k)){  
                  Ck += c
                }
              }
  		   
          }
      }
      Ck.toArray
    }
    else{  //如果上一次的Lk是频繁1项集，那么直接互连就行了。
      for(i<-0 to len_Lk-1){
        for(j<-0 to len_Lk-1){    
          val c = L_k(j).:::(L_k(i))
          if(!has_infrequent_subset(c,L_k))  
             Ck += c
        }
      }
      Ck.toArray
    }
	   
  }
  
   //判断新生成的k+1候选集是否包含非频繁k项集
  //一个项集，如果有至少一个非空子集是非频繁的，那么这个项集一定是非频繁的。需要预先去掉，就不用再进行下一步的频率统计了

  def has_infrequent_subset(candidate : List[String] , L_k : Array[List[String]]): Boolean ={
	  if(!L_k.contains(candidate.init))
       return true          
	  else if(!L_k.contains(candidate.tail))
       return true         
	  else
	    return false
  }
}




//最原始根据账号group之后组成的列表是：
//List(E, C, A)
//List(C, D, C, A)
//List(D, E)
//List(A, B, C, D)
//List(A, B, C, D, E)
//db count:5 minSupport:2 Lk count: 5

//Round: 1
//(List(E),3)
//(List(D),4)
//(List(B),2)
//(List(A),4)
//(List(C),5)
//Round: 2
//(List(C, A),2)
//(List(C, D),3)
//(List(A, B),2)
//(List(D, E),2)
//(List(B, C),2)
//Round: 3
//(List(A, B, C),2)
//(List(B, C, D),2)
//Round: 4
//(List(A, B, C, D),2)
//Lk_last: 
//List(A, B, C, D)

//注意本程序是严格按照直接连接关系才算是频繁项，比如 A, B, C  中， A, B是频繁项，而 A, C 不是频繁项。      如果要考虑A, C 也是频繁项的话，程序又要改一改
