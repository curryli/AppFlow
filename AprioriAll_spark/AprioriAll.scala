package com.spark


import org.apache.spark.{SparkContext, SparkConf}

import org.apache.spark.SparkContext._

import scala.collection.mutable.ArrayBuffer
 
object Apriori {
  def main (args: Array[String]){

    val conf = new SparkConf()
    conf.setAppName("Apriori")
    //conf.setMaster("local")

    val sc = new SparkContext(conf)

    val data = sc.textFile("ApriAll.csv")
    val startTime = System.currentTimeMillis()
	
	var arrdata = lines.map{line=>
	  val arr = line.split(",")
	  (arr(0),(arr(1),arr(2).toLong)
	}
	
	val createCombiner = (v: (String,Long) => List(v))
	val mergeValue =  (c : List[(String,Long)], v : (String,Long)) => 
		{
			var templist = v :: c,
			var	sortedlist = templist.sortBy(f=>(f._2))  //根据时间排序
			sortedlist.map(_1)   //排序之后只保留item
		}

	val mergeCombiners =  (c1: List[(String,Long)], c2: List[(String,Long)]) => 
		{
			var templist = c1 ::: c2,
			var	sortedlist = templist.sortBy(f=>(f._2))
			sortedlist.map(_1)
		}
	
	val seqRDD = arrdata.combineByKey(  
		createCombiner,
		mergeValue,
		mergeCombiners
	).map(temp => temp._2)   //把user去掉，现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	
	 
	
    val db = seqRDD.map(each =>Set(each.toString)).reduce( _ ++ _)).cache()   //split with " "
    val minSupport = ( db.count() * args(0).toDouble ).toInt

	//1频繁集项
    var Lk = lines.flatMap(_.split(" ")).map(word => (List(word.toString), 1)).reduceByKey(_ + _).filter(_._2 >= minSupport).cache()   //收集K_1项集

    var Lk_last = Lk.map( kv => kv._1)
    var sign = 0
    while(Lk.count() != 0){
      sign = sign + 1
      Lk.saveAsObjectFile(args(2) + sign.toString)
      Lk_last = Lk.map( kv => kv._1)
      val L_k = Lk_last.collect()
      val Candidate_k = AprioriGen(L_k)
      val Breadcast_Ck = sc.broadcast(Candidate_k)
      Lk = db.flatMap{   //对新生成的候选集进行频率统计（分布式统计），筛选出大于支持度的item
        kv =>
          val temp = ArrayBuffer[(List[String] , Int)]()
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
  def AprioriGen(L_k: Array[List[String]]): Array[(List[String])] = {

    val Ck = ArrayBuffer[List[String]]()
    L_k.foreach{
      l1 =>   //因为是List，并且要按顺序拼接，  ABC, ABD => ABCD  AB是相似的，可能拼接成 ABCD 或者ABDC （对有顺序的来说这是不一样的）   
	           //另一种 ABC, BCD => ABCD  中间BC是相似的，可能拼接成 ABCD  
        L_k.foreach{
          l2 =>
            if( l1[1:] == l2[:-2]){
              val c = l1.init + l1[1:] + l2.last
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

  def has_infrequent_subset(candidate : List[String] , L_k : Array[List[String]]): Boolean ={
	candidate.subsets(candidate.size -1).foreach{
      s =>
        if(!L_k.contains(s))
          return true         //只要发现一个非频繁K子集，就跳出
    }
    return false
  }
}
