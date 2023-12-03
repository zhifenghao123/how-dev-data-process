package com.howdev.scala.oop;

/**
 * PackageTestJava class
 *
 * @author haozhifeng
 * @date 2023/12/02
 */
public class PackageTestJava {
    public static void main(String[] args) {
        /**
         * Java
         * 包：package
         * 用法：包名的规则
         *      域名的反写+ 项目的名称 + 模块的名称 + 程序的分类（分层）名称
         *      com.howdev + gmall + user + util(service, bean)
         * 用途：
         *  （1）分类管理
         *  （2）区分类：java.util.Date, java.sql.Date
         *  （3）包权限（默认同包可互相访问）
         *  （4）包的路径应该和存放路径相同
         *
         * 分析：User user = new User();
         *  （1）声明一个User，含义不明确，最好见名知意，类名起得准确一点。如UserUtil、UserService
         *  （2）直观上，从对象的声明也看不到包名。如果可以通过类名对类进行区分，那么其实包没有那么重要。
         *  （3）权限不好用（经常用的是public、private，包权限和protected几乎不会用）
         *  （4）包语法在执行过程中，只要从classpath环境变量中可以查找到，就可以，所以和源码的位置没有关系。
         *  （程序不是对源码中执行，而是对编译后的的class文件执行，只要保证能找到编译后的class就行）
         *
         *  Scala的package语法比java的语法更方便
         *  （1）package的关键字可以多次声明，体现不同的包关系
         *      java中的.表示从属关系
         *  （2）可以给包设定作用域，体现上下级关系
         *  （3）将包也可以当做对象。 package object oop
         *      在包对象中声明方法，在当前包和它的子包中都可以直接访问
         *  （4）包名和物理路径没有关系（虽然ideal会报错，但是可以编译，idea以java语法为基础）
         */

    }
}
