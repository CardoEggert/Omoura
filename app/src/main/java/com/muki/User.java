package com.muki;

/**
 * Created by Cardo on 26.11.2016.
 */

public class User {
    private String name;
    private Integer age;
    private Double weight;
    private Integer readyNess;

    public User(String name, Integer age, Double weight, Integer readyNess) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.readyNess = readyNess;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getReadyNess() {
        return readyNess;
    }
}

