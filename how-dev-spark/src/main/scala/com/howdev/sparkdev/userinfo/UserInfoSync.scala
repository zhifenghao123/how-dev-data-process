package com.howdev.sparkdev.userinfo

import org.apache.spark.sql.functions.{col, when}
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

    val replacedUserInfoDF2 = replacedUserInfoDF1.withColumn("gender", when(col("gender") === "NULL", null).otherwise(col("gender")))
    val replacedUserInfoDF3 = replacedUserInfoDF2.withColumn("age", when(col("age") === "NULL", null).otherwise(col
    ("age")))
    val replacedUserInfoDF4 = replacedUserInfoDF3.withColumn("phoneNumber", when(col("phoneNumber") === "NULL", null).otherwise(col
    ("phoneNumber")))
    val replacedUserInfoDF5 = replacedUserInfoDF4.withColumn("email", when(col("email") === "NULL", null).otherwise(col
    ("email")))


    val userInfoFinalDF = replacedUserInfoDF5;


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
