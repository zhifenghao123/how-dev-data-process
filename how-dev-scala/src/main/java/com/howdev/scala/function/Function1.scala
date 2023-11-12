package com.howdev.scala.function

/**
 * 普通版本的函数式编程使用
 */
object Function1 {
  def main(args: Array[String]): Unit = {

    testFuncVariableArgs

    testFuncArgsDefaultValue

    testFuncWithArgsName

  }

  // 函数的可变参数列表
  def testFuncVariableArgs() : Unit = {
    // 1.函数的参数个数没有限制，但是参数个数越多，传参越麻烦烦，不推荐
    // 2.函数没有方法一样的重载能力
    def fun1(name1 : String, name2 : String, name3 : String, name4 : String): Unit = {

    }

    // 3.函数参数的个数简化：可变参数，类似于Java中的...
    // 可变参数因为不确定，因此底层实现，采用了不同的数组集合使用,WrappedArray
    def fun2(name : String*) : Unit = {
      println(name)
    }
    fun2()
    fun2("zhangsan")
    fun2("zhangsan", "lisi")

    // 可变参数的位置：参数列表的最后一项
    // 一个参数列表中不可能有多个可变参数
    def fun3(passwd : String ,name : String*) : Unit = {
      println(name)
    }
  }

  // 函数参数的默认值(如果不传递，参数自动取默认值)
  // Scala函数的参数默认使用val声明
  def testFuncArgsDefaultValue() : Unit = {
    def fun1(name : String, password : String = "000000") = {
      println(s"name = ${name}, passwor = ${password}")
    }

    fun1("zhangsan")
    fun1("zhangsan", "11111")
  }


  // 函数参数的带参数名传递
  def testFuncWithArgsName() : Unit = {
    def fun1(name : String = "default_user", password : String = "000000") = {
      println(s"name = ${name}, passwor = ${password}")
    }

    fun1("zhangsan")
    fun1(password = "009999")
    // 如果想要改变传递参数的顺序，可以制定参数名
    fun1(password = "11111", name = "fffff")
  }




}
