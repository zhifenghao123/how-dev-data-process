package com.howdev.scala.oop

/**
 * Scala中的访问权限和Java类似（不完全相同），也有4种
 * （1）private：私有的，同类
 * （2）private：包权限，同类，同包
 * （3）protected：受保护权限，同类，同包，子类
 * （4）（default）：公共的，任何地方都可以使用
 *
 */
object Oop5AcessScala {
  def main(args: Array[String]): Unit = {

  }

  class User{
    private var name = "haozf"
    private[oop] var age = 20
    protected var email = "haozhifeng@163.com"
    var address = "beijing"

    def testFunc() = {
      println(this.name)
      println(this.age)
      println(this.email)
      println(this.address)

    }
  }

  class Emp{
    def testFunc() = {
      var user = new User()
      // println(user.name)
      println(user.age)
      // println(user.email)
      println(user.address)
    }
  }

  class Student extends User {
    def testFunc1() = {
      var user = new User()
      // println(user.name)
      println(user.age)
       println(user.email)
      println(user.address)
    }
  }
}


package subFieldPackage {

  import com.howdev.scala.oop.Oop5AcessScala.User

  class SubClass {
    def testFunc() = {
      var user = new User()
      // println(user.name)
      println(user.age)
      // println(user.email)
      println(user.address)
    }
  }
}