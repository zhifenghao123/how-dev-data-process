package com.howdev.scala.oop;

/**
 * Oop3ClassJava class
 *
 * @author haozhifeng
 * @date 2023/12/03
 */
public class Oop3ClassJava {
    public static void main(String[] args) {
        /**
         * Java
         * （1）一个源码文件中，类可以声明多个，但是公共类只能有一个，而且和源文件名相同。
         * （2）抽取对象的相同内容（模版）
         * （3）使用class声明类
         *
         * Scala和Java的class语法大同小异：
         * （1）Scala源码中所有声明的类都可以是公共的
         * （2）使用object也可以声明类，但是编译后，同时也会产生另外一个对象的类
         *      主要作用是:模拟静态语法
         *      （为什么要模拟静态语法？Java中是有静态语法的，但静态语法又不是面向对象的，应该将静态语法去掉；
         *      但是去掉静态语法，就不能和Java说的通了。
         *      要模拟静态语法的话，就需要有一个对象，使用 对象名称.方法名称 执行，这个对象名称恰恰就是类名。
         *      可以理解为 User User = new User();    User名称和类名完全一样，调用对象方法和调用静态语法一起用）
         *
         *      使用object关键字可以编译为两个类文件，这两个类文件之间有关系：
         *      一般情况下，将类似于Java中的成员方法和属性声明在object声明类中，
         *      将静态方法和属性声明在object声明类的另一个类（声明类名 加 $）中；
         *      将object声明的类称之为伴生类，将object声明的对象称之为伴生对象。 伴生类和半生对象可以同时出现在一个源码文件中。
         */
    }
}
