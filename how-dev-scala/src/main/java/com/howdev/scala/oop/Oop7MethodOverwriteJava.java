package com.howdev.scala.oop;

/**
 * Oop7MethodOverwriteJava class
 *
 * @author haozhifeng
 * @date 2023/12/03
 */
public class Oop7MethodOverwriteJava {
    public static void main(String[] args) {
        /***
         * 	方法的重写其实就是在同一个内存区域中存在两个一样的方法，该如何区分的问题?
         * JVM在调用对象的成员方法时， 会遵循动态绑定机制：
         * 所谓的动态绑定机制，就是在方法运行时，将方法和当前运行对象的实际内存进行绑定。然后调用。
         * 动态绑定机制和属性没有任何关系，属性在哪里声明在哪里使用
         */
        CC cc = new DD();
        System.out.println(cc.add());
    }
}

class CC {
    public int i = 10;
    public int add() {
        //return i + 10;
        return getI() + 10;
    }

    public int getI() {
        return i;
    }
}

class DD extends CC{
    public int i = 20;

//    public int add() {
//        return i + 20;
//    }

    public int getI() {
        return i;
    }
}
