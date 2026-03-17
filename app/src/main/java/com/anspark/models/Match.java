package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class Match {
    @SerializedName("id")
    private String id;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("liked")
    private boolean liked;

    @SerializedName("matched_at")
    private String matchedAt;

    public Match() {
    }

    public Match(String id, Profile profile, boolean liked, String matchedAt) {
        this.id = id;
        this.profile = profile;
        this.liked = liked;
        this.matchedAt = matchedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(String matchedAt) {
        this.matchedAt = matchedAt;
    }
}
