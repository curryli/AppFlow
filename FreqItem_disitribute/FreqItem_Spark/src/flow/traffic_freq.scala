package flow


import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import scala.collection.immutable.SortedSet
import scala.collection.mutable.ArrayBuffer

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

import java.util.Date
import org.apache.spark.mllib.fpm.PrefixSpan
import scala.collection.mutable.ArrayBuffer


object traffic_freq {
  def main (args: Array[String]){

    val conf = new SparkConf()
    conf.setAppName("AprioriSeq")
    //conf.setMaster("local")

    val sc = new SparkContext(conf)

    val m_ratio = 0.2
    val out_dir = "xrli/FreqItem/APPout/"
   
    //val lines = sc.textFile("xrli/CardholderTag/traffic_sh/")
    
    val lines = sc.textFile("hdfs://nameservice1/user/hive/warehouse/00012900_shanghai.db/xrli_diff")
    
    
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 
    
     var arrdata = lines.map(line=>{
	   val arr = line.split("\\001")
	   var time = new Date()
	   try {
	      time = format.parse(arr(5))
	   }catch {  
       case ex:  java.text.ParseException => time = format.parse("0000-00-00 00:00:00.0")
     }  
	   
	   var month =  time.getMonth.toString()
	   var date =  time.getDate.toString()
	   
	   ((arr(0), (month + "-" + date) )  , (arr(1),time))   //(卡号  pdate), (MCC,时间)
	  }
	)
	
	val createCombiner = ( (v : (String,Date)) => List[(String,Date)](v) )
	
	val mergeValue =  (c : List[(String,Date)], v : (String,Date)) => 
		{
			var templist = v :: c
			var	sortedlist = templist.sortBy(f=>(f._2))  //根据时间排序
			sortedlist   //排序之后只保留item
		}

	val mergeCombiners =  (c1: List[(String,Date)], c2: List[(String,Date)]) => 
		{
			var templist = c1 ::: c2
			var	sortedlist = templist.sortBy(f=>(f._2))
			sortedlist
		}
	
	val seqRDD = arrdata.combineByKey(  
		createCombiner,
		mergeValue,
		mergeCombiners
	) 
	
	//seqRDD.take(100).foreach(println)
	
	  
	
	var sequences =  seqRDD.map(temp=>temp._2.map(p=>Array(p._1)).toArray)   //把user去掉，并且list 的每一个item把时间去掉，     现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	 
  println("total sequences cpunt is: " + sequences.count())
    
    val prefixSpan = new PrefixSpan()
      .setMinSupport(0.001)
      .setMaxPatternLength(5)
      
      
    val model = prefixSpan.run(sequences)
    
    model.freqSequences.collect().foreach { freqSequence =>
      println(
        s"${freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]")}," +
          s" ${freqSequence.freq}")
    }  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//	var arrdata = lines.map(line=>{
//	   val arr = line.split("\\001")
//	   var time = format.parse(arr(5))
//	   var month =  time.getMonth.toString()
//	   var date =  time.getDate.toString()
//	   
//	   ((arr(0), (month + "-" + date) )  , (arr(1),arr(2),arr(3),time))   //(卡号  pdate), (MCC,商户ID,商户名,时间)
//	  }
//	)
//	
//	val createCombiner = ( (v : (String,String,String,String)) => List[(String,String,String,String)](v) )
//	
//	val mergeValue =  (c : List[(String,String,String,String)], v : (String,String,String,String)) => 
//		{
//			var templist = v :: c
//			var	sortedlist = templist.sortBy(f=>(f._4))  //根据时间排序
//			sortedlist   //排序之后只保留item
//		}
//
//	val mergeCombiners =  (c1: List[(String,String,String,String)], c2: List[(String,String,String,String)]) => 
//		{
//			var templist = c1 ::: c2
//			var	sortedlist = templist.sortBy(f=>(f._4))
//			sortedlist
//		}
//	
//	val seqRDD = arrdata.combineByKey(  
//		createCombiner,
//		mergeValue,
//		mergeCombiners
//	) 
//	
//	
//	seqRDD.take(100).foreach(println)
	
    }
	   
  }
  
 
