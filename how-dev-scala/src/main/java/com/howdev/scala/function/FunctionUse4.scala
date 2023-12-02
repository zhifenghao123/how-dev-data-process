package com.howdev.scala.function

import scala.util.control.Breaks

object FunctionUse4 {
  def main(args: Array[String]): Unit = {
    testFuncitonAbstractControl()
  }

  // 抽象控制
  def testFuncitonAbstractControl() = {
    // 函数式编程语言-抽象控制
    def test1(f : () => Unit) = {
      f()
    }
    // 匿名函数
    test1(() => {
      println("dddd")
    })


    // f函数类型只有返回，没有输入的场合，称之为抽象，因为不完整
    // 调用的时候，就不能使用小括号
    // 在传值的时候，就需要进行控制
    def test2(f : => Unit) = {
      f
    }

    // 相比于上面匿名函数的调用，只有代码了
    // 所谓的控制抽象，其实就是将代码作为抽象传递
    // 而完整的参数传递，是将函数对象作为参数传递
    test2(
      println("dddddd")
    )

    // 一般的业务代码中几乎不用，但是在自定义语法时可以采用控制抽象，因为代码是可以传递的，也就意味着逻辑是变化的
    // 循环中断的代码就体现了控制抽象
    // 如果参数穿越多行，（）可以修改为{}
    Breaks.breakable{
      for (i <- 1 to 5) {
        if (i == 3) {
          Breaks.break()
        }
        println("i = " + i)
      }
    }
  }
}
