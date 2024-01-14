package com.howdev.sparklearn.sparkcore.rdd.serial

import com.howdev.sparklearn.SimpleSparkContext
import org.apache.spark.rdd.RDD

/**
 * Rdd序列化
 * 1) 闭包检查
 * 从计算的角度, 算子以外的代码都是在Driver端执行, 算子里面的代码都是在Executor端执行。
 * 那么在scala的函数式编程中，就会导致算子内经常会用到算子外的数据，这样就形成了闭包的效果，
 * 如果使用的算子外的数据无法序列化，就意味着无法传值给Executor端执行，就会发生错误，
 * 所以需要在执行任务计算前，检测闭包内的对象是否可以进行序列化，这个操作我们称之为闭包检测。
 * Scala2.12版本后闭包编译方式发生了改变
 *
 * 2) 序列化方法和属性
 * 从计算的角度, 算子以外的代码都是在Driver端执行, 算子里面的代码都是在Executor端执行
 *
 */
case object SerializableFunction extends SimpleSparkContext {

  def main(args: Array[String]): Unit = {
    val rdd: RDD[String] = sparkContext.makeRDD(Array("hello world", "spark", "hive", "haozhifeng"))
    val search = new Search("hello")
    // 函数传递，打印： 函数传递，打印： ERROR Task not serializable
    search.getMatch1(rdd).collect().foreach(println)

    // 属性传递，打印： ERROR Task not serializable
    search.getMatch2(rdd).collect().foreach(println)

  }

}

// class Search(query:String) {  因为没有序列化会报错
// 在Scala中，类的构造参数其实是类的属性，构造参数需要进行闭包检测，其实等同于类需要进行闭包检测
// case class Search(query:String) {
class Search(query:String) extends Serializable {
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

