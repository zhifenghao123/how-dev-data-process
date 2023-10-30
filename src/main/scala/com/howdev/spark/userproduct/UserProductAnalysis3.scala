package com.howdev.spark.userproduct

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{collect_list, struct}

object UserProductAnalysis3 {
  def main(args: Array[String]): Unit = {
    // 创建SparkSession
    val spark = SparkSession.builder()
      .master("local")
      .appName("UserProductAnalysis")
      .getOrCreate()

    // 读取订单表数据
    //val orders = spark.read.csv("test-datasets/user_product.csv")
    val ordersDf = spark.read
      .option("header", "true")
      .csv("test-datasets/user_product2.csv")

    ordersDf.show()

    // 对数据分组并聚合商品信息
    val groupedOrdersDf = ordersDf.groupBy("user_id", "product_type")
      .agg(collect_list(struct("product_id", "product_name", "product_price")).alias("product_info_list"))

    // 展示结果
    groupedOrdersDf.show()

    val groupedOrdersByUseridDf = groupedOrdersDf.groupBy("user_id")
      .agg(collect_list(struct("product_type", "product_info_list")).alias("u_product"))

    groupedOrdersByUseridDf.show()

    groupedOrdersByUseridDf.createOrReplaceTempView("groupedOrdersByUseridBase")

    println("--------------------")

    val frame = spark.sql(
      """
        |select *
        |from
        |groupedOrdersByUseridBase
        |""".stripMargin)
    frame.show()

    val backTrackCmidSet = frame.select("user_id").collect().toSet
    val customerIdString = backTrackCmidSet.mkString("(", ", ", ")")
    println(customerIdString)


  }

}
