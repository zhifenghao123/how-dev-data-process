package com.howdev.scala.oop;

/**
 * Oop6MethodOverloadJava class
 *
 * @author haozhifeng
 * @date 2023/12/03
 */
public class Oop6MethodOverloadJava {
    public static void main(String[] args) {
        /**
         * 方法的重载：
         * （1）方法名称相同
         * （2）方法参数列表相同（数量、参数类型、顺序）
         */

        AA aa = new AA();
        test(aa);

        BB bb = new BB();
        test(bb);

        // 查找方法，以类型为基础进行查找，如果指定类型不存在，就会扩大类型（找父类）继续查找
        AA aa1 = new BB();
        test(aa1);
    }

    public static void test(AA aa) {
        System.out.println("aaaa");
    }
    public static void test(BB bb) {
        System.out.println("bbbb");
    }
}

class AA{

}

class BB extends AA{

}
