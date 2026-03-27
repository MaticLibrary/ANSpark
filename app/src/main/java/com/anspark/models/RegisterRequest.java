package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("passwordConfirm")
    private String passwordConfirm;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("birthDate")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @SerializedName("preference")
    private String preference;

    @SerializedName("city")
    private String city;

    public RegisterRequest() {
    }

    // Конструктор з усіма полями
    public RegisterRequest(String email, String password, String passwordConfirm,
                           String displayName, String birthDate, String gender,
                           String preference, String city) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.displayName = displayName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.preference = preference;
        this.city = city;
    }

    // Старий конструктор для сумісності (можна видалити, якщо не використовується)
    public RegisterRequest(String email, String password, String passwordConfirm) {
        this(email, password, passwordConfirm, null, null, null, null, null);
    }

    // Геттери
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public String getDisplayName() { return displayName; }
    public String getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public String getPreference() { return preference; }
    public String getCity() { return city; }

    // Сеттери
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setGender(String gender) { this.gender = gender; }
    public void setPreference(String preference) { this.preference = preference; }
    public void setCity(String city) { this.city = city; }
}