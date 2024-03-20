package com.howdev.sparklearn.sparkcore.rdd.serial

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * Kryo序列化框架
 * 参考地址: https://github.com/EsotericSoftware/kryo
 * Java的序列化能够序列化任何的类。但是比较重（字节多），序列化后，对象的提交也比较大。
 * Spark出于性能的考虑，Spark2.0开始支持另外一种Kryo序列化机制。Kryo速度是Serializable的10倍。
 * 当RDD在Shuffle数据的时候，简单数据类型、数组和字符串类型已经在Spark内部使用Kryo来序列化。
 * 注意：即使使用Kryo序列化，也要继承Serializable接口。
 *
 */
case object SerializableKryo {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf()
      .setAppName("SerializableKryo")
      .setMaster("local[*]")
      // 替换默认的序列化机制 替换默认的序列化机制
      .set(" spark.serializer spark.serializer ", "org.apache.spark.serializer.KryoSerializer ")
      // 注册需要使用 注册需要使用 kryo 序列化的自定义类 序列化的自定义类
      .registerKryoClasses(Array(classOf[Search]))

    val sparkContext: SparkContext = new SparkContext(conf)

    val rdd: RDD[String] = sparkContext.makeRDD(Array("hello world", "spark", "hive", "haozhifeng"))
    val search = new Search2("hello")
    // 函数传递，打印： 函数传递，打印： ERROR Task not serializable
    search.getMatch1(rdd).collect().foreach(println)

    // 属性传递，打印： ERROR Task not serializable
    search.getMatch2(rdd).collect().foreach(println)

  }

}

// class Search(query:String) {  因为没有序列化会报错
// 在Scala中，类的构造参数其实是类的属性，构造参数需要进行闭包检测，其实等同于类需要进行闭包检测
// case class Search(query:String) {
class Search2(query:String) extends Serializable {
  def isMatch(s: String): Boolean = {
    s.contains(query)
  }
  // 函数序列化案例
   def getMatch1 (rdd: RDD[String]): RDD[String] = {
    //rdd.filter(this.isMatch)
    rdd.filter(isMatch)
   }

  // // 属性序列化案例
  def getMatch2(rdd: RDD[String]): RDD[String] = {
    //rdd.filter(x => x.contains(this.query))
    rdd.filter(x => x.contains(query))
    // val q = query
    //rdd.filter(x => x.contains(q))
  }
}

