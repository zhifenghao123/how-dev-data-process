package com.howdev.flinklearn.dto;

public class User {
    private Long userId;
    private String gender;
    private Integer age;
    private Double salary;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
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
