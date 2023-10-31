package com.howdev.spark.userproduct

import org.apache.spark.sql.functions.{collect_list, struct}
import org.apache.spark.sql.{Row, SparkSession}

case class Product(productType: String, productId: String, productName: String, productPrice: String)

case class Product2(productType: String, productId: String, productName: String, productPrice: String)


object UserProductAnalysis2 {
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
      .csv("how-dev-spark/test-datasets/user_product2.csv")

    ordersDf.show()

    // 对数据分组并聚合商品信息
    val groupedOrdersDf = ordersDf.groupBy("user_id")
      .agg(collect_list(struct("product_type", "product_id", "product_name", "product_price")).alias
      ("product_info_list"))

    groupedOrdersDf.createOrReplaceTempView("groupedOrdersBase")

    println("--------------------")


    // udf函数一：convertToProductList：转换为Product集合
    def convertToList(list: Seq[Row]): Seq[Product] = {
      list.map { row =>
        Product(row.getAs[String]("product_type"), row.getAs[String]("product_id"), row.getAs[String]("product_name")
          , row.getAs[String]("product_price"))
      }
    }
    spark.udf.register("convertToProductList", (list: Seq[Row]) => {
      convertToList(list)
    })

    // udf函数二：convertToProductList：转换为Product2集合
    def convertToList2(list: Seq[Row]): String = {
      val product2List = list.map { row =>
        Product2(row.getAs[String]("product_type"), row.getAs[String]("product_id"), row.getAs[String]("product_name")
          , row.getAs[String]("product_price"))
      }

      var t = ""
      for(product2 <- product2List) {
        t += product2.productPrice
        t += "##"
      }
      t
    }

    spark.udf.register("convertToProduct2List", (list: Seq[Row]) => {
      convertToList2(list)
    })



    val frame = spark.sql(
      """
        |select
        | user_id,
        | convertToProductList(product_info_list),
        | convertToProduct2List(product_info_list)
        |from
        | groupedOrdersBase
        |""".stripMargin)
    frame.show()
  }
}

