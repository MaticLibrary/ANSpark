package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class Photo {
    @SerializedName("id")
    private String id;

    @SerializedName(value = "url", alternate = {"avatar_url"})
    private String url;

    @SerializedName("primary")
    private boolean primary;

    public Photo() {
    }

    public Photo(String id, String url, boolean primary) {
        this.id = id;
        this.url = url;
        this.primary = primary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
