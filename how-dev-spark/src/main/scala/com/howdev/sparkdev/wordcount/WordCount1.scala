package com.howdev.sparkdev.wordcount

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount1 {
  def main(args: Array[String]): Unit = {
    // 创建spark运行的配置信息
    // setMaster 设置运行集群(local为本地)
    // setAppName 设置job的name
    val sparkconf: SparkConf = new SparkConf().setMaster("local").setAppName("WordCountDemo")

    // 创建和Spark框架的链接
    val sc: SparkContext = new SparkContext(sparkconf)

    println("========原始数据===============")
    // 读取指定文件数据
    val txtFile: RDD[String] = sc.textFile("how-dev-spark/test-datasets/sparkdev/wordcount/word.txt")
    txtFile.foreach(println)

    println("======切割+扁平化处理==================")
    // 切割单词,扁平化处理
    val words: RDD[String] = txtFile.flatMap(
      _.split(" ")
    )
    words.foreach(println)

    println("=======分组===========================")
    // 分组
    val wordGrp: RDD[(String, Iterable[String])] = words.groupBy(word => word)
    wordGrp.foreach(println)

    println("=======统计===========================")

    def sum(pt: (String, Iterable[String])) = {
      (pt._1, pt._2.size)
    }

    //计数
    val result: RDD[(String, Int)] = wordGrp.map(sum)
    result.foreach(println)

    println("=======根据计数排序===========================")
    val sort = result.sortBy(pt => pt._2)
    sort.foreach(println)

    // 关闭连接
    sc.stop()
  }

}
