package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class Chat {
    @SerializedName("id")
    private String id;

    @SerializedName("participant")
    private Profile participant;

    @SerializedName("last_message")
    private Message lastMessage;

    @SerializedName("last_message_at")
    private String lastMessageAt;

    public Chat() {
    }

    public Chat(String id, Profile participant, Message lastMessage, String lastMessageAt) {
        this.id = id;
        this.participant = participant;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Profile getParticipant() {
        return participant;
    }

    public void setParticipant(Profile participant) {
        this.participant = participant;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(String lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
