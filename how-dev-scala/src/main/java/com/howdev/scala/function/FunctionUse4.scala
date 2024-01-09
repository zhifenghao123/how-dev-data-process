package com.howdev.scala.function

import scala.util.control.Breaks

object FunctionUse4 {
  def main(args: Array[String]): Unit = {
    testFuncitonAbstractControl()

    testFuncitonClosure()

    testFuncitonCurry()

    testFuncitonLaz()
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

  // 函数闭包
  def testFuncitonClosure() : Unit = {

     // 函数时编程语言-闭包
    // 自己的理解：闭包是一个环境，innerFunc包含了a，a被innerFunc包含进去后，别人用不了
    def outerFunc(a : Int) = {
      def innerFunc(b : Int ) = {
        a + b
      }
      innerFunc _
    }

    var inner = outerFunc(2)
    println(inner(4))

    // 闭包的实现，在不同的Scala版本中是不一样的
    // 在Scala 2.12之前，闭包使用的是匿名函数类完成的
    // 在Scala 2.12之后，闭包使用的是改变函数的声明完成的


    val name = "haozhifeng"
    def test() = {
      println(name)
    }
    // 没有造成闭包
    test()

    // 有了闭包，但不是因为name，而是因为test
    val f = test _
    f()

    //总结：闭包的场景
    // (1)内部函数使用了外部的数据，改变了数据的生命周期
    // (2)将函数作为对象使用，改变函数本身的生命周期
    // (3)所有的匿名函数都有闭包（匿名函数当对象了，符合了（2））
    // (4)内部函数返回到外部使用也会有闭包

  }

  // 函数式编程语言-函数柯里化（Curry）
  def testFuncitonCurry() : Unit = {

    def test(a : Int, b : Int) = {
      for(i <- 1 to a) {
        println(i)
      }

      for(i <- 1 to b) {
        println(i)
      }
    }

    val a1 = 3  // 需要2s
    val b1 = 4  // 需要15min


    // 函数的参数之间没有关联性，那么传值的时候要求同时传递，增加了耦合性，也增加了调用的难度
    // （a1很快计算完了，但是要一直等到有值才可以）
    test(a1, b1)

    // 所谓的柯里化，就是为了函数简单化，将无关的参数进行分离，可以设定多个参数列表
    def test2(a : Int)(b : Int) : Unit = {
      for(i <- 1 to a) {
        println(i)
      }

      for(i <- 1 to b) {
        println(i)
      }
    }

    val intToUnit : Int => Unit = test2(10)
    test2(10)(20)  // 在函数调用上，和内部函数的调用相似


  }

  // 惰性函数
  def testFuncitonLaz() : Unit = {
    def test() : String = {
      println("start exec test()")
      "haozhifeng"
    }

    // val a = test()
    lazy val a = test()
    println("----------")
    println(a)
  }
}
