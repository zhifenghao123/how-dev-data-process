package com.howdev.sparklearn.sparkcore.rdd.operator.transformation

import com.howdev.sparklearn.SimpleSparkContext
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

case object KeyValueTransformation extends SimpleSparkContext{
  def main(args: Array[String]): Unit = {
    // partitionByTest
    // reduceByKeyTest
    // groupByKeyTest
    // aggregateByKeyTest
    otherTest
  }

  /**
   * 1）函数签名
   *  def partitionBy(partitioner: Partitioner): RDD[(K, V)]
   *
   * 2）函数作用
   *  将数据按照指定Partitioner重新进行分区。Spark默认的分区器是HashPartitioner
   *
   */
  def partitionByTest(): Unit = {
    val dataRdd = sparkContext.makeRDD(List(1,2,3,4), 2)
    val dataRddMap = dataRdd.map((_, 1))

    val dataRddPartitionBy = dataRddMap.partitionBy(new HashPartitioner(2))
    dataRddPartitionBy.saveAsTextFile("how-dev-spark/test-datasets/output")

  }

  /**
   * 1）函数签名
   * def reduceByKey(func: (V, V) => V): RDD[(K, V)]
   * def reduceByKey(func: (V, V) => V, numPartitions: Int): RDD[(K, V)]
   *
   * 2）函数作用
   * 可以将数据按照相同的Key对Value进行聚合
   */
  def reduceByKeyTest(): Unit = {
    val dataRdd = sparkContext.makeRDD(List(("a", 1), ("a", 2),  ("a",3), ("b",4)))

    val dataRddReduceByKey = dataRdd.reduceByKey(_ + _)
    dataRddReduceByKey.collect().foreach(println)


    // reduceByKey中如果key的数据只有一个，是不参与运算的
    dataRdd.reduceByKey(
      (x: Int, y: Int) => {
        println("x = " + x + ",y =" + y)
        x + y
      })
      .collect().foreach(println)


  }

  /**
   * 1）函数签名
   * def groupByKey(): RDD[(K, Iterable[V])]
   * def groupByKey(numPartitions: Int): RDD[(K, Iterable[V])]
   * def groupByKey(partitioner: Partitioner): RDD[(K, Iterable[V])]
   *
   * 2）函数作用
   * 将数据源的数据根据key对value进行分组
   *
   * reduceByKey和groupByKey的区别？
   * 从shuffle的角度：
   * reduceByKey和groupByKey都存在shuffle的操作，但是reduceByKey可以在shuffle前对分区内相同key的数据进行预聚合（combine）功能，
   * 这样会减少落盘的数据量，而groupByKey只是进行分组，不存在数据量减少的问题，reduceByKey性能比较高。
   * 从功能的角度：
   * reduceByKey其实包含分组和聚合的功能。GroupByKey只能分组，不能聚合，所以在分组聚合的场合下，推荐使用reduceByKey，如果仅仅是分组而不需要聚合。那么还是只能使用groupByKey
   *
   */
  def groupByKeyTest(): Unit = {
    val dataRdd = sparkContext.makeRDD(List(("a", 1), ("a", 2),  ("a",3), ("b",4)))

    val dataRddGroupByKey = dataRdd.groupByKey()
    dataRddGroupByKey.collect().foreach(println)

  }

  /**
   * 1）函数签名
   * def aggregateByKey[U: ClassTag](zeroValue: U)(seqOp: (U, V) => U, combOp: (U, U) => U): RDD[(K, U)]
   * 2）函数说明
   * 将数据根据不同的规则进行分区内计算和分区间计算
   */
  def aggregateByKeyTest(): Unit = {
    val dataRdd = sparkContext.makeRDD(List(("a", 1), ("a", 2),  ("a",3), ("b",4)))

    // 取出每个分区内相同 取出每个分区内相同 key 的最大值然后分区间相加
    // aggregateByKey 算子是函数柯里化，存在两个参列表
    // 1. 第一个参数列表中的示初始值,主要用于碰见第一个key的时候，和value进行分区内计算
    // 2. 第二个参数 列表中含有两第二个参数 列表中含有两
    //  2.1 第一个参数表示分区内的计算规则 第一个参数表示分区内的计算规则
    //  2.2 第二个参数表示分区间的计算规则

    val resultRdd = dataRdd.aggregateByKey(0)(
      (x, y) => math.max(x, y),
      (x, y) => x + y
    )
    resultRdd.collect().foreach(println)
  }

  /**
   * 1.foldByKey
   *  1）函数签名
   *    def foldByKey(zeroValue: V)(func: (V, V) => V): RDD[(K, V)]
   *  2）函数说明
   *    当分区内计算规则和分区间计算规则相同时，aggregateByKey就可以简化为foldByKey
   *
   * 2.combineByKey
   *  1）函数签名
   *    def combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C, C) => C): RDD[(K, C)]
   *  2）函数说明
   *    最通用的对key-value型rdd进行聚集操作的聚集函数（aggregation function）。类似于aggregate()，combineByKey()允许用户返回值的类型与输入不一致。
   *  ？？？？：reduceByKey、foldByKey、aggregateByKey、combineByKey的区别？
   *  reduceByKey: 相同key的第一个数据不进行任何计算，分区内和分区间计算规则相同
   *  FoldByKey: 相同key的第一个数据和初始值进行分区内计算，分区内和分区间计算规则相同
   *  AggregateByKey：相同key的第一个数据和初始值进行分区内计算，分区内和分区间计算规则可以不相同
   *  CombineByKey:当计算时，发现数据结构不满足要求时，可以让第一个数据转换结构。分区内和分区间计算规则不相同。
   *
   * 3.sortByKey
   *  1）函数签名
   *    def sortByKey(ascending: Boolean = true, numPartitions: Int = self.partitions.length): RDD[(K, V)]
   *  2）函数说明
   *    在一个(K,V)的RDD上调用，K必须实现Ordered接口(特质)，返回一个按照key进行排序的
   *
   * 4.join
   *  1）函数签名
   *    def join[W](other: RDD[(K, W)]): RDD[(K, (V, W))]
   *  2）函数说明
   *    在类型为(K,V)和(K,W)的RDD上调用，返回一个相同key对应的所有元素连接在一起的(K,(V,W))的RDD
   *
   * 5.leftOuterJoin
   *  1）函数签名
   *    def leftOuterJoin[W](other: RDD[(K, W)]): RDD[(K, (V, Option[W]))]
   *  2）函数说明
   *    类似于SQL语句的左外连接
   *
   * 6.cogroup
   *  1）函数签名
   *    def cogroup[W](other: RDD[(K, W)]): RDD[(K, (Iterable[V], Iterable[W]))]
   *  2）函数说明
   *    在类型为(K,V)和(K,W)的RDD上调用，返回一个(K,(Iterable<V>,Iterable<W>))类型的RDD
   */
  def otherTest(): Unit = {
    val dataRdd = sparkContext.makeRDD(List(("a", 1), ("a", 2),  ("a",3), ("b",4)))
    val dataRDDFoldByKey = dataRdd. foldByKey(0)(_+_)

    // 将数据List(("a", 88), ("b", 95), ("a", 91), ("b", 93), ("a", 95), ("b", 98))求每个key的平均
    val list: List[(String, Int)] = List(("a", 88), ("b", 95), ("a", 91), ("b", 93), ("a", 95), ("b", 98))
    val input: RDD[(String, Int)] = sparkContext.makeRDD(list, 2)
    val combineRdd: RDD[(String, (Int, Int))] = input. combineByKey(
      (_, 1),
      (acc: (Int, Int), value: Int) => (acc._1 + value, acc._2 + 1),
      (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
    )
    combineRdd.collect().foreach(println)

  }
}
