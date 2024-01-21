package com.howdev.sparklearn.sparkcore.rdd.operator.transformation

import com.howdev.sparklearn.SimpleSparkContext

/**
 * 双Value类型RDD的Transformation
 */
case object ValueTransformation2  extends SimpleSparkContext{
  def main(args: Array[String]): Unit = {
    val dataRdd1 = sparkContext.makeRDD(List(1,2,3,4))
    val dataRdd2 = sparkContext.makeRDD(List(3,4,5,6))

    /**
     * 1）函数签名
     * def intersection(other: RDD[T]): RDD[T]
     * 2）函数说明
     * 对源RDD和参数RDD求交集后返回一个新的RDD
     */
    val dataRddIntersection = dataRdd1.intersection(dataRdd2)
    println(dataRddIntersection.collect() mkString ",")

    /**
     * 1）函数签名
     * def union(other: RDD[T]): RDD[T]
     * 2）函数说明
     * 对源RDD和参数RDD求并集后返回一个新的RDD
     */
    val dataRddUnion = dataRdd1.union(dataRdd2)
    println(dataRddUnion.collect() mkString ",")

    /**
     * 1）函数签名
     * def subtract(other: RDD[T]): RDD[T]
     * 2）函数说明
     * 以一个RDD元素为主，去除两个RDD中重复元素，将其他元素保留下来。求差集
     */
    val dataRddSubtract = dataRdd1.subtract(dataRdd2)
    println(dataRddSubtract.collect() mkString ",")

    /**
     * 1）函数签名
     * def zip[U: ClassTag](other: RDD[U]): RDD[(T, U)]
     * 2）函数说明
     * 将两个RDD中的元素，以键值对的形式进行合并。其中，键值对中的Key为第1个RDD中的元素，Value为第2个RDD中的相同位置的元素。
     *
     * 拉链操作两个RDD数据源的数据类型可以不一致
     * 两个RDD数据源的分区数量必须一致
     * 两个RDD数据源的分区中元素个数必须一致
     */
    val dataRddZip = dataRdd1.zip(dataRdd2)
    println(dataRddZip.collect() mkString ",")



  }

}
