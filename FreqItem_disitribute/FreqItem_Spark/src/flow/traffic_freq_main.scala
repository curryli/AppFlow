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


object traffic_freq_main {
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
	   
	   ((arr(0), (month + "-" + date) )  , (arr(1),arr(2),arr(3),time))   //(卡号  pdate), (MCC,商户ID,商户名,时间)
	  }
	)
	
	val createCombiner = ( (v : (String,String,String,Date)) => List[(String,String,String,Date)](v) )  
	
	val mergeValue =  (c : List[(String,String,String,Date)], v : (String,String,String,Date)) => 
		{
			var templist = v :: c
			var	sortedlist = templist.sortBy(f=>(f._4))  //根据时间排序
			sortedlist   //排序之后只保留item
		}

	val mergeCombiners =  (c1: List[(String,String,String,Date)], c2: List[(String,String,String,Date)]) => 
		{
			var templist = c1 ::: c2
			var	sortedlist = templist.sortBy(f=>(f._4))
			sortedlist
		}
	
	val seqRDD = arrdata.combineByKey(  
		createCombiner,
		mergeValue,
		mergeCombiners
	) 
	
	//seqRDD.take(100).foreach(println)
	
	  
	val traffic_mcd_list = List("105290000025237","105290000025238","105290000025239","105290000025240","301310041310009","301310041310010","113310041110003","113310041110004","113310041110005","113310041110006","104310041119134","104310041119135","104310041119136","102310041110010","102310041110011","102310041110012","301310041310011")
	var sequences =  seqRDD.map(temp=>temp._2)   //把user去掉，并且list 的每一个item把时间去掉，     现在每一个rdd就是一个序列  <A,B,C,D>   或者 <A,C,D>等等
	 
  sequences = sequences.filter {lst => !{
    val mcd_lst = lst.map(f=>f._2)
    mcd_lst.intersect(traffic_mcd_list).isEmpty 
    }
	}
     
	println("traffic related sequences count is: " + sequences.count())
	
	
	val MCC_seq = sequences.map(temp=> temp.map(x=>Array(x._1)).toArray)  
	val MCD_seq = sequences.map(temp=> temp.map(x=>Array(x._2)).toArray)  
  val MName_seq = sequences.map(temp=> temp.map(x=>Array(x._3)).toArray)  
	
	
	 
  println("Frequen MCC pattern in 0.0001:\n")
  val prefixSpan = new PrefixSpan()
      .setMinSupport(0.0001)
      .setMaxPatternLength(8)
      
      
  val model = prefixSpan.run(MCC_seq)
    
    model.freqSequences.collect().foreach { freqSequence =>
      println(
        s"${freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]")}," +
          s" ${freqSequence.freq}")
    }  
    
    

//	println("Frequen MCD pattern in 0.0001:\n")
//  val prefixSpan2 = new PrefixSpan()
//      .setMinSupport(0.0001)
//      .setMaxPatternLength(8)
//      
//      
//  val model2 = prefixSpan2.run(MCD_seq)
//    
//    model2.freqSequences.collect().foreach { freqSequence =>
//      println(
//        s"${freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]")}," +
//          s" ${freqSequence.freq}")
//    } 
    
    
	
  println("Frequen MName pattern in 0.0001:\n")
  val prefixSpan3 = new PrefixSpan()
      .setMinSupport(0.0001)
      .setMaxPatternLength(8)
      
      
  val model3 = prefixSpan3.run(MName_seq)
  

  var sb_list = List[StringBuilder]()
    
  model3.freqSequences.collect().foreach { freqSequence =>{
      val sb = new StringBuilder; 
      sb.append(s"${freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]")},").append(s" ${freqSequence.freq}").append("\n") 
      sb_list = sb_list.::(sb)
   }
  } 
    
  
  sc.parallelize(sb_list).saveAsTextFile("xrli/CardholderTag/traffic_Freq_MName")
    
    
    
    
    
    
    
    
    
    
    
    
     
    }
	   
  }
  
 
