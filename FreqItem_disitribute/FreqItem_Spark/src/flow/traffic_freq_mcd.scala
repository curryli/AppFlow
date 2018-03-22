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


object traffic_freq_mcd {
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
	   
	   ((arr(0), (month + "-" + date) )  , (arr(2),time))   //(卡号  pdate), (mcd,时间)
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
	
	  
	val traffic_mcd_list = List("105290000025237","105290000025238","105290000025239","105290000025240","301310041310009","301310041310010","113310041110003","113310041110004","113310041110005","113310041110006","104310041119134","104310041119135","104310041119136","102310041110010","102310041110011","102310041110012","301310041310011")
	var sequences =  seqRDD.map(temp=>temp._2.map(p=>p._1))   //把user去掉，并且list 的每一个item把时间去掉，     现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	 
  sequences = sequences.filter {lst => !lst.intersect(traffic_mcd_list).isEmpty }
	
	println("total sequences cpunt is: " + sequences.count())
	
	
  val seq_rdd = sequences.map(temp=> temp.map{x => Array(x)}.toArray )   //把user去掉，并且list 的每一个item把时间去掉，     现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	  
	 
    val prefixSpan = new PrefixSpan()
      .setMinSupport(0.01)
      .setMaxPatternLength(5)
      
      
    val model = prefixSpan.run(seq_rdd)
    
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
  
 
