package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class MatchResponse {
    @SerializedName("matchId")
    private Long matchId;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}