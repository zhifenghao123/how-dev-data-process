package com.howdev.scala.oop

import scala.beans.BeanProperty

object Oop4FieldScala {
  def main(args: Array[String]): Unit = {

    val user = new User
    user.name = "hao"

    println(user.email)

    user.setName("hhh")
  }


  /**
   * (1)使用val声明属性，在编译时，会给属性加上final关键字
   * (2)类的属性在编译时，都是private权限
   *    类的属性在编译时，会同时生成公共的set、get方法，但是方法名不是以set和get开头的
   *    当访问属性时，等同于调用对象的get方法；当给属性赋值时，等同于调用对象的set方法；
   *
   *    java中Bean对象有bean的开发规范：
   *    属性私有化，提供公共的set、get方法，且方法名以set、get开头；
   *    scala中对象属性不遵循java中bean的规范。这样的话，和其他的技术开发框架集成，就会有问题
   *    为了解决Scala和其他开发框架集成使用的问题，Scala提供了一个注解，解决Bean规范的问题
   */
  class User{
    @BeanProperty var name : String = _
    val age : Int = 30
    val email : String = "haozhifeng@163.com"
  }
}
