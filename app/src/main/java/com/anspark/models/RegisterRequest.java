package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("age")
    private int age;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("city")
    private String city;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, int age, String email, String password, String city) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.password = password;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
