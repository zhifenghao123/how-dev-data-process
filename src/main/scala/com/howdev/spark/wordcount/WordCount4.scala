package com.howdev.spark.wordcount

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// 一行代码 实现wordcount
object WordCount4 {
  def main(args: Array[String]): Unit = {
    // 创建spark运行的配置信息
    // setMaster 设置运行集群(local为本地)
    // setAppName 设置job的name
    val sparkconf: SparkConf = new SparkConf().setMaster("local").setAppName("WordCountDemo")

    // 创建和Spark框架的链接
    val sc: SparkContext = new SparkContext(sparkconf)

    val result: RDD[(String, Int)] = sc.textFile("test-datasets/word.txt")
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _).
      sortBy(_._2)

    result.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
