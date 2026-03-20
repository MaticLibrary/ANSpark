package com.anspark.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Profile {
    @SerializedName("id")
    private String id;

    @SerializedName(value = "display_name", alternate = {"name"})
    private String displayName;

    @SerializedName("age")
    private Integer age;

    @SerializedName("bio")
    private String bio;

    @SerializedName("city")
    private String city;

    @SerializedName("birth_date")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @SerializedName("preference")
    private String preference;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("photos")
    private List<Photo> photos;

    public Profile() {
    }

    public Profile(Profile other) {
        if (other == null) {
            return;
        }
        id = other.id;
        displayName = other.displayName;
        age = other.age;
        bio = other.bio;
        city = other.city;
        birthDate = other.birthDate;
        gender = other.gender;
        preference = other.preference;
        avatarUrl = other.avatarUrl;
        tags = other.tags != null ? new ArrayList<>(other.tags) : null;
        photos = other.photos != null ? new ArrayList<>(other.photos) : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return displayName;
    }

    public void setName(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getAge() {
        if (age != null && age > 0) {
            return age;
        }
        return calculateAgeFromBirthDate(birthDate);
    }

    public Integer getAgeValue() {
        return age;
    }

    public void setAge(int age) {
        this.age = age > 0 ? age : null;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
        if ((age == null || age <= 0) && birthDate != null && !birthDate.trim().isEmpty()) {
            int calculatedAge = calculateAgeFromBirthDate(birthDate);
            age = calculatedAge > 0 ? calculatedAge : null;
        }
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public String getPrimaryImageUrl() {
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            return avatarUrl;
        }
        if (photos != null && !photos.isEmpty() && photos.get(0) != null) {
            return photos.get(0).getUrl();
        }
        return null;
    }

    private int calculateAgeFromBirthDate(String rawBirthDate) {
        if (rawBirthDate == null || rawBirthDate.trim().isEmpty()) {
            return 0;
        }

        try {
            LocalDate birth = LocalDate.parse(rawBirthDate);
            LocalDate now = LocalDate.now();
            if (birth.isAfter(now)) {
                return 0;
            }
            return Period.between(birth, now).getYears();
        } catch (DateTimeParseException ignored) {
            return 0;
        }
    }
}
