package com.howdev.sparklearn.sparkcore.rdd.transformation

import com.howdev.sparklearn.SimpleSparkContext
import org.apache.spark.rdd.RDD

object ValueTransformation extends SimpleSparkContext{
  def main(args: Array[String]): Unit = {
    //mapTest()
    mapPartitionsTest
  }

  /**
   * 1）函数签名
   * def map[U: ClassTag](f: T => U): RDD[U]
   * 2）函数说明
   * 将处理的数据逐条进行映射转换，这里的转换可以是类型的转换，也可以是值的转换。
   */
  def mapTest(): Unit = {
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4))
    val dataRdd1: RDD[Int] = dataRdd.map(
      num => {
        num * 2
      }
    )

    val dataRdd2: RDD[String] = dataRdd.map(
      num => {
        "" + num
      }
    )

    dataRdd1.collect().foreach(println)

    dataRdd2.collect().foreach(println)
  }

  /**
   * 1）函数签名
   * def mapPartitions[U: ClassTag](
   *    f: Iterator[T] => Iterator[U],
   *    preservesPartitioning: Boolean = false): RDD[U]
   * 2）函数说明
   * 将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处理，哪怕是过滤数据。
   * 3）思考一个问题：map和mapPartitions的区别？
   * a）数据处理角度
   * Map算子是分区内一个数据一个数据的执行，类似于串行操作。
   * 而mapPartitions算子是以分区为单位进行批处理操作。
   * b）功能的角度
   * Map算子主要目的将数据源中的数据进行转换和改变。但是不会减少或增多数据。
   * MapPartitions算子需要传递一个迭代器，返回一个迭代器，没有要求的元素的个数保持不变，所以可以增加或减少数据
   * c）性能的角度
   * Map算子因为类似于串行操作，所以性能比较低，而是mapPartitions算子类似于批处理，所以性能较高。
   * 但是mapPartitions算子会长时间占用内存，那么这样会导致内存可能不够用，出现内存溢出的错误。所以在内存有限的情况下，不推荐使用。使用map操作。
   *
   */
  def mapPartitionsTest(): Unit ={
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4))

    val dataRdd1: RDD[Int] = dataRdd.mapPartitions(
      datas => {
        datas.filter(_ == 2)
      }
    )
    dataRdd1.collect().foreach(println)

  }

  /**
   * 1）函数签名
   * def mapPartitionsWithIndex[U: ClassTag](
   *    f: (Int, Iterator[T]) => Iterator[U],
   *    preservesPartitioning: Boolean = false): RDD[U]
   * 2）函数说明
   *  将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处理，哪怕是过滤数据，在处理时同时可以获取当前分区索引。
   *
   */
  def mapPartitionsWithIndex(): Unit ={
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4))

//    val dataRdd1 = dataRdd.mapPartitionsWithIndex(
//      (index, datas) => {
//        datas.map( index , _)
//      }
//    )
//
//    dataRdd1.collect().foreach(println)

  }
}
