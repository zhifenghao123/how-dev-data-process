package com.howdev.scala.datatype

object StringTest {
  def main(args: Array[String]): Unit = {
    // Scala语言中没有字符串，使用的就是Java中的String
    // Java字符串的方法都可以在Scala中使用
    val name: String = "haozhifeng"
    val age: Int = 28

    println("{\"name\":\"haozhifeng\",\"age\":28}")

    // 1.字符串拼接
    println("name:" + name)
    println("{\"name\":\"" + name+ "\",\"age\":" + age + "}")

    // 2.传值字符串
    printf("{\"name\":\"%s\",\"age\":%s}", name, age)

    // 3.插值字符串
    // 字符串拼接Json格式，直接使用查值字符串会报错
    // println(s"{\"name\":\"$name\",\"age\":$age}") 会报错

    // 使用多行字符串
    // 束线|表示顶格
    // 多行字符串主要用于Sql和Json格式字符串
    println(
      """
        |hello
        |scala
        |""".stripMargin)

    println(
      """
        hello
        |scala
        |""".stripMargin)

    println(
      """
        #hello
        #scala
        #""".stripMargin('#'))

    println(
      """
        #hello
        #scala
        #""".stripMargin)

    println(
      s"""
        |{"name":"${name}","age":$age}
        |""".stripMargin)

  }
}
