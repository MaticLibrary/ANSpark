package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("profile")
    private Profile profile;

    // Пустий конструктор для GSON
    public AuthResponse() {
    }

    // Конструктор з 4 параметрами
    public AuthResponse(String token, String userId, String email, Profile profile) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.profile = profile;
    }

    // Геттери
    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Profile getProfile() {
        return profile;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}