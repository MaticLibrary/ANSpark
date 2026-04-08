package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id")
    private String id;

    @SerializedName(value = "match_id", alternate = {"chat_id"})
    private String chatId;

    @SerializedName(value = "sender_profile_id", alternate = {"sender_id"})
    private String senderId;

    @SerializedName(value = "content", alternate = {"text"})
    private String text;

    @SerializedName(value = "sent_at", alternate = {"created_at", "createdAt"})
    private String createdAt;

    @SerializedName("outgoing")
    private boolean outgoing;

    public Message() {
    }

    public Message(String id, String chatId, String senderId, String text, String createdAt, boolean outgoing) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.text = text;
        this.createdAt = createdAt;
        this.outgoing = outgoing;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
    }
}
