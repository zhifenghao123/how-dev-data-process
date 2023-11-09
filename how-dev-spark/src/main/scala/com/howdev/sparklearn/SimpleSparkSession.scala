package com.howdev.sparklearn

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SimpleSpark extends BaseEnvNew {
  @transient
  protected implicit lazy final val spark = {
/*    SparkSession.builder()
      .master("local[*]")
      .appName(appName)
      .getOrCreate()*/

    val builder = SparkSession.builder
    builder config sparkConf getOrCreate
  }

  @transient
  protected implicit lazy final val sparkContext = spark.sparkContext
}

trait BaseEnvNew extends Serializable {
  private var _appName = this.getClass.getSimpleName.filter(!_.equals('$'))

  protected final def appName: String = _appName
  protected def setAppName(name: String) = _appName = name

  protected def sparkConfOpts: Map[String, String] = Map.empty

  protected def sparkConf = {
    val sparkConf = new SparkConf().setAppName(_appName).setAll(sparkConfOpts)
    sparkConf
  }
}
