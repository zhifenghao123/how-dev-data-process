package com.howdev.spark.wordcount

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// 使用spark实现的reduceBykey方法
object WordCount3 {
  def main(args: Array[String]): Unit = {
    // 创建spark运行的配置信息
    // setMaster 设置运行集群(local为本地)
    // setAppName 设置job的name
    val sparkconf: SparkConf = new SparkConf().setMaster("local").setAppName("WordCountDemo")

    // 创建和Spark框架的链接
    val sc: SparkContext = new SparkContext(sparkconf)

    println("========FileInput读取切片数据===============")
    // 读取指定文件数据
    val txtFile: RDD[String] = sc.textFile("test-datasets/word.txt")
    txtFile.foreach(println)

    println("======Mapper类处理切片数据,每行调用一次map方法==================")
    println("======map方法输出(key,value),到环形缓冲区,按照key排序==================")
    // 切割单词,扁平化处理
    val words: RDD[(String, Int)] = txtFile.flatMap(
      _.split(" ")
    ).map((_, 1)).sortBy(_._1)

    words.foreach(println)

    //reduceByKey
    /*
    *  reduceByKey 方法
    *     说明
    *        将集合中按照相同的key,对value聚合,将多个key相同的元素 聚合成一个
    *     参数
    *        前后两个value聚合的规则(匿名函数/函数指针)
    *
    * */
    println("========使用reduceByKey方法==========")
    val result: RDD[(String, Int)] = words.reduceByKey(_ + _).sortBy(_._2)
    result.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
