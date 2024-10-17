package com.howdev.flinklearn.biz.domain;

import lombok.Data;

@Data
public class User {
    private String gender;
    private Integer age;
    private long registerTimeStamp;

    public User() {
    }

    public User(String gender, Integer age, long registerTimeStamp) {
        this.gender = gender;
        this.age = age;
        this.registerTimeStamp = registerTimeStamp;
    }

    @Override
    public String toString() {
        return "User{" +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", registerTimeStamp=" + registerTimeStamp +                '}';
    }
}
