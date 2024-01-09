package com.howdev.scala.function

/**
 * 进阶版的函数式编程使用
 *
 * Scala语言的作者马丁为了使开发人员更便捷地开发程序，简化了很多的代码
 * 如果编译器可以动态识别的语法，那么开发人员是不需要编写的，可以省略
 * 这体现了【至简原则】，能省就省
 */
object FunctionUse2 {

  def main(args: Array[String]): Unit = {
    testFuncAdvanced
  }


  def testFuncAdvanced() : Unit = {
    // 1.如果函数体中的逻辑代码需要返回，可以省略return关键字
    // Scala语言中，所有表达式可以直接将满足条件的最后一行的逻辑代码的结果作为返回值返回
    def test1(): String = {
      "haozhifeng"
    }
    print(test1())

    // 2.如果函数的逻辑代码只有一行，那么可以将大括号省略掉
    def test2() : String = "haozhifeng2"
    print(test2())

    // 3.如果可以通过函数体的返回值推断出返回类型，那么返回值类型也可以省略
    def test3() = "haozhifeng3"
    print(test3())

    // 4.如果函数参数列表中没有声明参数，那么函数小括号也可以省略
    // 如果省略了函数参数的小括号列表，那么调用时也不能添加。 print(test4()) 会报错
    // 因为省略了很多语法内容，所以声明变量和声明函数很像，所以需要通过关键字区别
    def test4 = "haozhifeng4"
    print(test4)

    // 5.如果函数体中的逻辑代码中有return语句，但是函数声明为Unit，此时return不起作用
    def test5(): Unit = {
      return "haozhifeng5"
    }
    print(test5())
    // 此时如果想要省略Unit，那么必会发生错误，如果既想要省略Unit，又不希望发生错误，可以将等号同时省略
    // 等号可以省略，但是一般是和Unit一起省略
    def test5_1() {
      return "haozhifeng6"
    }
    println(test5_1())

    // 6.如果函数名不重要的时候，def和函数名可以一起省略，称之为匿名函数
    // (1)def和函数名要一起省略
    // (2)返回值类型也要省略，由函数体代码自动推断
    // (3)等号需要增加大于号表示关联
      println(() => {
        return "haozhifeng6"
      })

  }
}
