package com.howdev.sparkdev.userinfo

import org.apache.spark.sql.catalyst.dsl.expressions.StringToAttributeConversionHelper
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserInfoSync {
  def main(args: Array[String]): Unit = {

    // 创建SparkSession
    val spark = SparkSession.builder()
      .master("local")
      .appName("UserInfoSync")
      .getOrCreate()


    val jdbcUrl = "jdbc:mysql://localhost:3306/h_order"  // MySQL数据库连接URL
    val username = "root"  // MySQL用户名
    val password = "root"  // MySQL密码
    val tableName = "user_info"  // MySQL表的名称

    val userInfoDF = spark.read
      .option("emptyString", "")
      .option("charset", "UTF-8")
      .option("header", "true")
      .csv("how-dev-spark/test-datasets/sparkdev/userinfo/user_info.csv")

    val replacedUserInfoDF1 = userInfoDF.na.fill("", Seq("userName", "password", "nickName"))

    // 将age列中值为"null"的替换为默认值1
/*    val replacedUserInfoDF2 = replacedUserInfoDF1.withColumn("age", when(col("age").equals("null"), 1).otherwise(col
    ("age")))*/

    // 将gender列中值为"null"的替换为默认值unknown
/*    val userInfoFinalDF = replacedUserInfoDF2.withColumn("gender", when(col("gender").equals("null"), "unknown")
      .otherwise(col("gender")))*/

    val userInfoFinalDF = replacedUserInfoDF1;


    userInfoFinalDF.show()
// userId,userName,password,gender,age,phoneNumber,email,nickName,registerTime,createTime,updateTim
    val selectedCols = userInfoFinalDF
      .select("userId", "userName", "password", "gender", "age", "phoneNumber",
        "email", "nickName", "registerTime", "createTime", "updateTime")
    //selectedCols.na.fill("")


    selectedCols
      .withColumnRenamed("userId", "user_id")
      .withColumnRenamed("userName", "user_name")
      .withColumnRenamed("phoneNumber", "phone_number")
      .withColumnRenamed("nickName", "nick_name")
      .withColumnRenamed("registerTime", "register_time")
      .withColumnRenamed("createTime", "create_time")
      .withColumnRenamed("updateTime", "update_time")
      .write
      .format("jdbc")
      .option("url", jdbcUrl)
      .option("dbtable", tableName)
      .option("user", username)
      .option("password", password)
      .option("useUnicode", "true") // 设置字符集为UTF-8
      .option("characterEncoding", "UTF-8") // 设置字符集为UTF-8
      .mode(SaveMode.Append)  // 根据需要选择覆盖或追加模式
      .save()
  }
}
