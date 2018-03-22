package Algorithm


import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.immutable.SortedSet
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.fpm.PrefixSpan
import scala.collection.mutable.ArrayBuffer

  
object prefixspan {
  def main (args: Array[String]){

    val conf = new SparkConf()
    conf.setAppName("prefixspan")
 
    val sc = new SparkContext(conf)

//    val sequences = sc.parallelize(Seq(
//      Array(Array(1, 2), Array(3)),
//      Array(Array(1), Array(3, 2), Array(1, 2)),
//      Array(Array(1, 2), Array(5)),
//      Array(Array(6))
//    ), 2).cache()
    
    
    val lines = sc.textFile("xrli/FreqItem/prefix_test.data")
    
    val sequences = lines.map(_.split(",")).map(item=>Array(item))
    
    
    val prefixSpan = new PrefixSpan()
      .setMinSupport(0.5)
      .setMaxPatternLength(5)
      
      
    val model = prefixSpan.run(sequences)
    
    model.freqSequences.collect().foreach { freqSequence =>
      println(
        s"${freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]")}," +
          s" ${freqSequence.freq}")
    }
        
  }
  
}
 

//  <(12)3>
//  <1(32)(12)>
//  <(12)5>
//  <6>