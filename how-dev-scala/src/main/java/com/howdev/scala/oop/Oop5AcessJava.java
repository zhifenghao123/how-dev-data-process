package com.howdev.scala.oop;

/**
 * Oop5AcessJava class
 *
 * @author haozhifeng
 * @date 2023/12/03
 */
public class Oop5AcessJava {
    public static void main(String[] args) throws CloneNotSupportedException {
        /***
         * 权限是什么？权利和限制
         * Java权限：以方法为例，方法的提供者和方法的调用者之间的关系，决定了访问权限
         * （1）private：私有的，同类
         * （2）default：包权限，同类，同包
         * （3）protected：受保护权限，同类，同包，子类
         * （4）public：公共的，同类，同包，子类，所有类
         *
         *
         * Scala中的访问权限和Java类似（不完全相同），也有4种
         * （1）private：私有的，同类
         * （2）private：包权限，同类，同包
         * （3）protected：受保护权限，同类，同包，子类
         * （4）（default）：公共的，任何地方都可以使用
         *
         */

        AAA aaa = new AAA();
        /**
         * clone()方法来自于Object，所以AAA对象有clone()方法
         * clone()方法提供者：java.lang.Object  (如果重写的话，就是com.howdev.scala.oop.AAA， 和调用者同包)
         * clone()方法调用者：com.howdev.scala.oop.Oop5AcessJava
         *
         */
        // 这里的. 不是调用的意思，表示从属关系
        aaa.clone();
    }

}

class AAA{
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}