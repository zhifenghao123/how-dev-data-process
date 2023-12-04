package com.howdev.scala.oop

object Oop7InstanceScala {
  def main(args: Array[String]): Unit = {
    /**
     * （1）通过new构建对象
     * （2）通过反射构建对象
     * （3）通过clone构建对象
     * （4）通过序列化构建对象
     * （5）Scala中可以通过object关键字构建对象
     *    println(Oop7InstanceScala)
     * （6）Scala中的apply()方法也可以构建对象，实际上采用的就是new，但是编译器可以动态识别
     *     apply方法可以省略，集合对象基本上都是采用这种方式构建
     */
    println(Oop7InstanceScala)

    val user = Oop7User.apply();
    // apply方法可以省略，集合对象基本上都是采用这种方式构建
    val user1 = Oop7User()

    println(user)
    println(user1)

  }
}

class Oop7User{

}
object Oop7User{
  def apply(): Oop7User = new Oop7User()
}
