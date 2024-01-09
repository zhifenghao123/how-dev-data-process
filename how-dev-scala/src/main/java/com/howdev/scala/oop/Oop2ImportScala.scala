package com.howdev.scala.oop
object Oop2ImportScala {
  def main(args: Array[String]): Unit = {
    // 1.可以导包
    import com.howdev.scala.oop
    oop.testPackageObjectFunc()

    // 2.import关键字可以在任何地方导入

    // 3.Scala导入一个包中的所有类，采用下划线代替java中的星号
    import java.util._

    // 4.可以将一个包中的多个类在同一行导入
    import java.io.{InputStream, OutputStream}

    // 5.屏蔽类（如果不同的包中有相同的类名，可以通过屏蔽方式进行编译）
    import java.sql.{Date=>_, _}
    println(new Date())

    // 6、给类起别名
    import java.util.{HashMap=>JavaHashMap}
    val map = new JavaHashMap()

    // 7、Scala中import的规则
    //print(new _root_.java.util.HashMap())

  }

}

/*
package java{
  package util{
    class HashMap{

    }
  }
}
*/
