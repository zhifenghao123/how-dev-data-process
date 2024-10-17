package com.howdev.flinklearn.biz.bo;

import com.howdev.flinklearn.biz.domain.User;

public class UserGenerator {

    private static String[] genders = {"M", "F"};

    public static User generate(Long userId) {

        String gender = genders[(int) (Math.random() * genders.length)];
        Integer age = (int) (Math.random() * 100);
        return generate(gender, age, System.currentTimeMillis());
    }
    public static User generate(String gender, Integer age, long registerTimeStamp) {
        User user = new User();
        user.setGender(gender);
        user.setAge(age);
        user.setRegisterTimeStamp(registerTimeStamp);
        return user;
    }


    public static void main(String[] args) {
        User user = UserGenerator.generate(1L);
        System.out.println(user);
    }
}
