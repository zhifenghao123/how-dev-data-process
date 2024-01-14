package com.howdev.sparklearn.sparkcore.rdd.operator.action

import com.howdev.sparklearn.SimpleSparkContext
import org.apache.spark.rdd.RDD

/**
 * 行动算子，触发作业Job执行的算子
 * 底层代码调用的是环境对象的runJob方法
 * 底层代码中会创建ActiveJob对象，并提交执行
 */
case object ActionOperation extends SimpleSparkContext {
  def main(args: Array[String]): Unit = {
    val rdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4))
    // reduce():聚集RDD中的所有元素，先聚合分区内数据，再聚合分区间数据
    val i = rdd.reduce(_ + _)
    println(i)

    // collect():会将不同分区的数据按照分区顺序采集到Driver端内存中，行程数组（在驱动程序中，以数组Array的形式返回数据集的所有元素）
    val i1 = rdd.collect()
    println(i1.mkString(","))

    // count:返回RDD中元素的个数
    // first:返回RDD中的第一个元素
    // take:返回RDD中前n个元素
    // takeOrdered:返回RDD中前n个元素，按照默认排序规则
    // aggragate:分区的数据通过初始值和分区内的数据进行聚合，然后再和初始值进行分区间的数据聚合
    // fold:聚合操作，需要传入初始值和函数
    // countByKey:统计每种key的个数
    // forEach:分布式遍历RDD中的每一个元素，调用指定函数

    // save 相关算子
    // def saveAsTextFile(path: String): Unit
    // def saveAsObjectFile(path: String): Unit
    // def saveAsSequenceFile(path: String, codec: Option[Class[_ <: CompressionCodec]] = None): Unit
    //保存成 Text 文件
    rdd. saveAsTextFile ("output")
    // 序列化成对象保存到文件
    rdd. saveAsObjectFile ("output1")
    // 保存成 Sequencefile 文件,要去数据格式必须为 key-value 格式
    rdd.map((_,1)). saveAsSequenceFile("output2")





  }
}
