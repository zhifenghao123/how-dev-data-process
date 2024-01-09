package com.howdev.sparklearn.sparkcore.rdd.builder

import com.howdev.sparklearn.SimpleSparkContext

/**
 * 在Spark中，创建RDD的创建方式可以分为 四种：
 * 1)  从集合（内存）中创建 RDD
 *  从集合中创建RDD，Spark主要提供了两个方法：parallelize和makeRDD
 *  从底层代码实现来讲，makeRDD方法其实就是parallelize方法
 * 2) 从外部存储（文件）创建 RDD
 *  由外部存储系统的数据集创建RDD包括：本地的文件系统，所有Hadoop支持的数据集，比如HDFS、HBase等。
 * 3) 从其他 RDD 创建
 *  主要是通过一个RDD运算完后，再产生新的RDD。
 * 4) 直接创建 RDD （new new）
 *  使用 new的方式直接构造RDD，一般由Spark框架自身使用。
 *
 *
 *  默认情况下，Spark可以将一个作业切分多个任务后，发送给Executor节点并行计算，而能够并行计算的任务数量我们称之为并行度。
 *  这个数量可以在构建RDD时指定。记住，这里的并行执行的任务数量,并不是指的切分任务的数
 *  1）读取内存数据时，数据可以按照并行度的设定进行数据的分区操作
 *  2）读取文件数据时，数据是按照Hadoop文件读取的规则进行切片分区，而切片规则和数据读取的规则有些差异
 * */
object RddBuilder extends SimpleSparkContext{

  def main(args: Array[String]): Unit = {
    // testRddCreatedByMemory
    testRddCreatedByFile
  }

  def testRddCreatedByMemory(): Unit = {
    val rdd1 = sparkContext.parallelize(List(1, 2, 3, 4))
    val rdd2 = sparkContext.makeRDD(List(1, 2, 3, 4))

    rdd1.collect().foreach(println)
    rdd2.collect().foreach(println)

    sparkContext.stop()
  }

  def testRddCreatedByFile(): Unit = {
    val fileRdd = sparkContext.textFile("how-dev-spark/test-datasets/word.txt")

    fileRdd.collect().foreach(println)

    sparkContext.stop()
  }

}
