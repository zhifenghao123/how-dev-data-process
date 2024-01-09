package com.howdev.scala.function

/**
 * 高阶版的函数式编程使用
 *
 * Scala是一个完全面向对象编程的语言，所以一切皆对象
 * Scala是一个完全面向函数式编程的语言，所以一切皆函数
 *
 * 也就是在Scala中，函数即对象，对象即函数
 *
 * java：
 * Class User{
 *  private String name;
 *  ...
 * }
 * User user = new User()
 *
 * Scala:在Scala中声明了一个函数，相当于声明了一个函数对象
 * def test() {
 * ...
 * }
 *
 *
 */
object FunctionUse3 {

  def main(args: Array[String]): Unit = {
    //testFuncAdvanced
    //testFuncAdvanced2
    //testFuncAdvanced3
    //testFuncAdvanced4
    testFuncAdvanced5

  }

  def testFuncAdvanced() : Unit = {
    def test(): Unit = {
    }

    def testArg1(age: Int): String = {
      age.toString
    }

    println(test) // 打印test方法的执行结果

    // 如果不想让函数执行，只想访问这个函数本身，可以采用特殊符号进行转化
    println( test _ )

    // Scala函数其实就是对象
    // 1.对象应该有类型
    // 2.对象应该可以赋值给其他人使用

    // 此时，函数对象test赋值给f
    val f = test _
    // 编译器可以自动推断出f的类型，函数对象的类型可以称之为函数类型
    // Function0[Unit]
    //  这里类型中的0表示函数参数列表中的参数个数
    //  中括号中的Unit表示函数没有返回值
    val f1: Function0[Unit] = test _

    // 上面使用下划线的目的是不让函数执行，而将他作为对象是用。如果能明确知道函数不执行，那就可以省略下划线
    val f1_1: () => Unit = test



    // Function1[Int, String]
    //  这里类型中的1表示函数参数列表中的参数个数
    //  中括号中的Int表示函数参数为Int类型
    //  中括号中的Int表示函数返回值为String类型
    val f2: Function1[Int, String] = testArg1 _

    // 【函数对象】（把函数当做对象）的参数最多只能有22个，最多对应到Function22
    // 函数的参数格式不受限制


    // 为了使用方便，函数类型可以使用另一种声明方式
    // 这里的函数类型为：Int(参数列表的参数类型) => String(返回值类型)
    val f3 = testArg1 _



  }

  def testFuncAdvanced2() : Unit = {
    def test(): Unit = {
    }

    def test2(name: String, age: Int): String = {
      println(s"name = ${name}, age = ${age}")
      s"${name}___${age}"
    }
    // 将函数赋值给一个变量，这个变量就是函数，这个变量传参后可移执行
    val f1 = test2 _
    f1("hao", 20)

    // 在执行f2，即使没有函数参数，也需要()
    val f2 = test _
    f2()
    println(f2)
  }

  // 将函数对象作为参数使用
  def testFuncAdvanced3() : Unit = {
    def test(): Unit = {
      println("... test()")
    }

    def test2(name: String, age: Int): String = {
      println(s"name = ${name}, age = ${age}")
      s"${name}___${age}"
    }
    val f1 = test _

    val f2 = test2 _

    def testFunObj(f: () => Unit) = {
      f()
    }

    def testFunObj2(f: (String, Int) => String): Unit = {
      val str = f("hhhhh", 21)
      println(str)
    }

    testFunObj(f1)
    // 将函数对象作为参数传递，就类似于将逻辑进行传递，逻辑不用写死了
    testFunObj2(f2)



    def sum(x: Int, y: Int) : Int = x + y
    def diff(x: Int, y: Int) : Int = x - y

    def processData(f: (Int, Int) => Int) : Unit = {
      val result = f(10, 20)
      println(result)
    }

    // TODO:下划线的省略
    // 将函数名作为参数传递给另一个函数时，不需要使用下划线
    // 使用下划线的目的是不让函数执行，而将他作为对象是用。如果能明确知道函数不执行，那就可以省略下划线
    processData(sum)
    processData(diff)

    // TODO:函数的名称真的重要吗？
    // 如果将函数对象作为参数使用时，那么参数的名称很重要，因为调用时使用的是参数名称
    // 传递到函数的名称不重要，因此可以考虑去掉传递的函数的名称
    // 如果传递的函数没后名称和def关键字，那其实就是匿名函数，一般就是作为参数使用
    processData((x: Int, y: Int) => {
      x * 10 + y
    })
    // 匿名函数用作参数时，可以采用至简原则
    // (1)匿名函数的逻辑代码只有一行，可以省略大括号
    processData((x: Int, y : Int) => x * 10 + y)
    // (2)匿名函数的参数类型可以推断出来，匿名函数参数类型也可以省略
    processData((x, y) => x * 10 + y)
    // (3)匿名函数的参数个数只有1个，那么参数小括号也可以省略
    // (4)匿名函数中如果参数按照顺序只执行一次的场合，那么可以使用下划线代替参数，省略参数列表和箭头
    processData(_ * 20 + _)


    def calc(x: Int, f : (Int, Int) => Int, y: Int) :Int = {
      f(x, y)
    }

    //def sum(x: Int, y: Int) = x + y
    //def -(x: Int, y: Int) = {x - y}


    println(calc(3, sum, 5))
    //println(calc(3, -, 5))
    println(calc(3, _ - _, 5))

  }


  def testFuncAdvanced4() : Unit = {
    // TODO:Scala也可以将函数对象作为返回结果
    // 函数的返回值类型一般情况下不声明，使用自动推断类型

    def outerFunc() = {
      def innerFunc(): Unit = {
        println("do innerFunc")
      }
      innerFunc _
    }


    // 此时，f就是一个函数对象，函数类型是：() => Unit
    val f1 = outerFunc()
    f1()

    // 也可以直接这样用
    outerFunc()()


    // 如果一个函数使用了外部的变量，但是改变这个变量的生命周期，
    // 将这个变量包含到当前函数的作用于内，形成了闭合的环境，这个环境称之为闭包环境
    // 简称闭包（函数时编程语言中的概念）
    def outerFunc1(x: Int) = {
      def mid(f: (Int, Int) => Int) = {
        def inner(y: Int) = {
          f(x, y)
        }
        inner _
      }
      mid _
    }

    val mid = outerFunc1(10)
    val inner = mid(_ + _)
    val result = inner(20)
    println(result)

    println(outerFunc1(10)(_ * _)(20))

  }

  def testFuncAdvanced5() : Unit = {

    // 匿名函数
    def testFun(f: String => Unit): Unit = {
      f("haoyu")
    }

    def fun(name: String): Unit = {
      println(s"name = ${name}")
    }

    testFun(fun)

    testFun(
      (name: String) => { println(s"name = ${name}")}
    )

    testFun(
      (name: String) => println(s"name = ${name}")
    )

    // 参数按照顺序只使用一次，可以使用下划线代替，但是不能嵌套使用
    // testFun(println(s"name = _")) 会报错
    testFun(
      _.substring(1)
    )

    // 匿名函数的至简原则的最简单版本
    testFun(
      println(_)
    )
    // 不是匿名函数的至简原则的最简单版本，只是巧了
    testFun(
      println
    )



  }
}
