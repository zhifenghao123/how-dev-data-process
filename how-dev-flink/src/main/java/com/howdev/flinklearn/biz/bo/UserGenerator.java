package com.howdev.flinklearn.biz.bo;

import com.howdev.flinklearn.biz.domain.User;

public class UserGenerator {

    private static String[] genders = {"M", "F"};

    public static User generate(Long userId) {

        String gender = genders[(int) (Math.random() * genders.length)];
        Integer age = (int) (Math.random() * 100);
        Double salary = Math.random() * 10000;
        return generate(userId, gender, age, salary, System.currentTimeMillis());
    }
    public static User generate(Long userId, String gender, Integer age, Double salary, long registerTimeStamp) {
        User user = new User();
        user.setUserId(userId);
        user.setGender(gender);
        user.setAge(age);
        user.setSalary(salary);
        user.setRegisterTimeStamp(registerTimeStamp);
        return user;
    }


    public static void main(String[] args) {
        User user = UserGenerator.generate(1L);
        System.out.println(user);
    }
}
