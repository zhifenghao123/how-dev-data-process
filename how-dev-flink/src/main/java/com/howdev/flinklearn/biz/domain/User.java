package com.howdev.flinklearn.biz.domain;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private String gender;
    private Integer age;
    private Double salary;
    private long registerTimeStamp;

    public User() {
    }

    public User(Long userId, String gender, Integer age, Double salary, long registerTimeStamp) {
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.salary = salary;
        this.registerTimeStamp = registerTimeStamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                '}';
    }
}
