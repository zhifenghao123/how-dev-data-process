package com.howdev.sparklearn.ext

import com.howdev.sparklearn.SimpleSpark
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}

import java.text.SimpleDateFormat

object SparkSqlRowTest extends SimpleSpark{
  def main(args: Array[String]): Unit = {

    val rows: Seq[Row] = Seq(
      Row(1L, "event1", "2023-03-02 10:00:00.0"),
      Row(2L, "event2","2023-01-01 09:01:00.0"),
      Row(3L, "event1", "2023-02-01 11:00:00.0"),
      Row(4L, "event3", "2023-04-01 16:00:00.0"),
      Row(5L, "event2","2023-01-01 09:00:00.0")
    )

    val schema: StructType = StructType(Seq(
      StructField("id", DataTypes.LongType, true),
      StructField("event", DataTypes.StringType, true),
      StructField("date", DataTypes.StringType, true)
    ))

    // test1(rows)
    test2(rows, schema)

  }

  /**
   * 测试 Spark SQL Row 类型
   * @param rows
   */
  def test1(rows: Seq[Row]): Unit = {
    // 将日期字符串转换为 Date 类型
    def parseDateString(dateStr: String): java.util.Date = {
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      format.parse(dateStr)
    }

    // 获取时间属性列索引（这里假设时间属性在第二列）
    val dateColumnIndex = 2

    // 通过 sortBy 函数按照时间属性进行排序
    // val sortedRows = rows.sortBy(row => row.getAs[java.util.Date](dateColumnIndex))
    val sortedRows = rows.sortBy(row => parseDateString(row.getString(dateColumnIndex)))

    sortedRows.foreach(println(_)) // 输出结果

    println("-----------------------")
    val map = rows.groupBy(row => row.getString(1))
    println(map)
  }

  def test2(rows: Seq[Row], schema: StructType): Unit = {
    val rdd = spark.sparkContext.parallelize(rows)
    val df = spark.createDataFrame(rdd, schema)
    df.show()
  }


}
