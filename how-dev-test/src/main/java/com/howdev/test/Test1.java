package com.howdev.test;

import lombok.Data;

/**
 * Java、Python、JavaScript和Ruby具有如下特征：
 *
 * 如果对函数传递类的实例且修改其字段值，修改会作用于所传递的变量。
 * 如重新赋值参数，修改不会作用于所传入的变量。
 * 如使用nil/null/None传递参数值，将参数设为其它值不会修改调用函数中的变量。
 */
public class Test1 {
    public static void main(String[] args) {
        Person p = new Person();
        p.setName("zhangsan");
        p.setAge(18);
        test1(p);
        System.out.println(p.getName());
        test2(p);
        System.out.println(p.getName());
    }

    static void test1(Person p) {
        p.setName("lisi");
    }

    static void test2(Person p) {
        p = new Person();
        p.setName("wangwu");
        p.setAge(25);
    }


    @Data
    static class Person {
        private String name;
        private int age;
    }
}
