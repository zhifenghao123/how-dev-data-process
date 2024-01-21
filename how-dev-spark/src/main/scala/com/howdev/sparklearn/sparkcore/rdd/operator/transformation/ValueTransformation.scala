package com.howdev.sparklearn.sparkcore.rdd.operator.transformation

import com.howdev.sparklearn.SimpleSparkContext
import org.apache.spark.rdd.RDD

import java.text.SimpleDateFormat

object ValueTransformation extends SimpleSparkContext{
  def main(args: Array[String]): Unit = {
    //mapTest()
    //mapPartitionsTest
    // mapPartitionsWithIndexTest
    // flatMapTest
    // glomTest
    groupBytest
    filterAndSampleAndDistnictAndCoalesceAndRepartitionAndSortBy
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
  def mapPartitionsWithIndexTest(): Unit ={
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4), 2)
    // [1, 2] [3, 4]

    // 返回1号分区的所有数据
    val dataRddOfPartition1 = dataRdd.mapPartitionsWithIndex(
      // 分区索引编号，该分区的所有数据
      (index, iter) => {
        if (index == 1) {
          iter
        } else {
          Nil.iterator
        }
      }
    )

    dataRddOfPartition1.collect().foreach(println)

    println("----")

    val dataRdd2: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4, 5, 6))
    // 每一个数据以及对应所在的分区号
    val partitionIndexToDataRdd = dataRdd2.mapPartitionsWithIndex(
      (index, iter) => {
        iter.map(
          num => {
            (num, index)
          }
        )
      }
    )
    partitionIndexToDataRdd.collect().foreach(println)

  }


  /**
   * 1）函数签名
   *  def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U]
   * 2）函数说明
   *  将处理的数据进行扁平化后再进行映射处理，所以算子也称之为扁平映射
   */
  def flatMapTest(): Unit = {
    val dataRdd : RDD[List[Int]] = sparkContext.makeRDD(List(List(1, 2), List(4, 5)))
    val dataRddFlatMapRdd = dataRdd.flatMap(
      list => list
    )
    dataRddFlatMapRdd.collect().foreach(println)


    val dataRdd2 : RDD[String] = sparkContext.makeRDD(List("Hello Scala", "Hello Spark"))
    val dataRdd2FlatMapRdd = dataRdd2.flatMap(
      str => str.split(" ")
    )
    dataRdd2FlatMapRdd.collect().foreach(println)

    // 将List(List(1,2),3,List(4,5))进行扁平化操作
    val dataRdd3 = sparkContext.makeRDD(List(List(1, 2), 3, List(4, 5)))
    val dataRdd3FlatMapRdd = dataRdd3.flatMap(
      data => data match {
        case list: List[Int] => list
        // case _ => List()
        case dataTemp => List(dataTemp)
      }
    )
    dataRdd3FlatMapRdd.collect().foreach(println)

  }

  /**
   * 1）函数签名
   *  def glom(): RDD[Array[T]]
   * 2）函数说明
   *  将同一个分区的数据直接转换为相同类型的内存数组进行处理，分区不变
   */
  def glomTest(): Unit = {
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4), 2)
    val dataRddGlomRdd: RDD[Array[Int]] = dataRdd.glom()
    dataRddGlomRdd.collect().foreach(data => println(data.mkString(",")))

    // 计算所有分区最大值求和（分区内取最大值，分区间最大值求和）
    val maxRdd : RDD[Int] = dataRddGlomRdd.map(
      arr => arr.max
    )
    println(maxRdd.collect().sum)
  }

  /**
   * 1）函数签名
   *  def groupBy[K](f: T => K)(implicit kt: ClassTag[K]): RDD[(K, Iterable[T])]
   * 2)函数说明
   *  将数据根据指定的规则进行分组, 分区数量默认不变，但是数据会被打乱重新组合，我们将这样的操作称之为shuffle。
   *  极限情况下，数据可能被分在同一个分区中
   *  注：一个组的数据在一个分区中，但是并不是说一个分区中只有一个组
   */
  def groupBytest(): Unit = {
    val dataRdd: RDD[Int] = sparkContext.makeRDD(List(1, 2, 3, 4), 2)
    def groupByFun(num: Int) = {
      num % 2
    }
    val dataRddGroupBy: RDD[(Int, Iterable[Int])] = dataRdd.groupBy(
      groupByFun
      )
    dataRddGroupBy.collect().foreach(println)

    val dataRdd2: RDD[String] = sparkContext.makeRDD(List("Hell0", "Spark", "Scala", "Java", "Hadoop"), 2)
    val dataRddGroupBy2 = dataRdd2.groupBy(_.charAt(0))
    dataRddGroupBy2.collect().foreach(println)

    println("------------")
    val apacheLogRdd = sparkContext.textFile("test-datasets/sparklearn/apache.log")
    apacheLogRdd.collect().foreach(println)

    val hourRdd: RDD[(String, Iterable[(String, Int)])] = apacheLogRdd.map(
      line => {
        println(line)
        val fields = line.split(" ")
        val dateTimeStr = fields(3)
        val dateTime = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss").parse(dateTimeStr)
        val hour: String = new SimpleDateFormat("HH").format(dateTime)
        (hour, 1)
      }
    ).groupBy(_._1)

    hourRdd.collect().foreach(println)

    hourRdd.map {
      case (hour, iter) => {
        (hour, iter.size)
      }
    }.collect().foreach(println)
  }

  /**
   * 1.filter
   * 1）函数签名
   *  def filter(f: T => Boolean): RDD[T]
   * 2)函数说明
   *  将数据根据指定的规则进行筛选过滤，符合规则的数据保留，不符合规则的数据丢弃。当数据进行筛选过滤后，分区不变，但是分区内的数据可能不均衡，生产环境下，可能会出现数据倾斜。
   *
   * 2.
   * 1）函数签名
   *  def sample(withReplacement: Boolean, fraction: Double, seed: Long = Utils.random.nextLong): RDD[T]
   * 2)函数说明
   *  根据指定的规则从数据集中抽取数据
   *
   * 3.distinct
   * 1）函数签名
   *  def distinct()(implicit ord: Ordering[T] = null): RDD[T]
   *  def distinct(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T]
   * 2)函数说明
   *  将数据集中重复的数据去重
   *
   *  4.coalesce
   *  1）函数签名
   *  def coalesce(numPartitions: Int, shuffle: Boolean = false,
   *    partitionCoalescer: Option[PartitionCoalescer] = Option.empty)
   *    (implicit ord: Ordering[T] = null)
   *     : RDD[T]
   *  2)函数说明
   *    根据数据量缩减分区，用于大数据集过滤后，提高小数据集的执行效率当spark程序中，
   *    存在过多的小任务的时候，可以通过coalesce方法，收缩合并分区，减少分区的个数，减小任务调度成本
   *
   *  5.repartition
   *  1）函数签名
   *  def repartition(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T]
   *  2)函数说明
   *  该操作内部其实执行的是coalesce操作，参数shuffle的默认值为true。无论是将分区数多的RDD转换为分区数少的RDD，
   *  还是将分区数少的RDD转换为分区数多的RDD，repartition操作都可以完成，因为无论如何都会经shuffle过程。
   *
   *  6.sortBy
   *  1）函数签名
   *    def sortBy[K](f: (T) => K,
   *    ascending: Boolean = true,
   *    numPartitions: Int = this.partitions.length)
   *    (implicit ord: Ordering[K], ctag: ClassTag[K]): RDD[T]
   *  2)函数说明
   *  该操作用于排序数据。在排序之前，可以将数据通过f函数进行处理，之后按照f函数处理的结果进行排序，默认为升序排列。
   *  排序后新产生的RDD的分区数与原RDD的分区数一致。中间存在shuffle的过程
   */
  def filterAndSampleAndDistnictAndCoalesceAndRepartitionAndSortBy(): Unit = {
    println("。。。。。")
  }
}
