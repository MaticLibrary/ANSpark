package com.anspark.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginationResponse<T> {
    @SerializedName("data")
    private List<T> data;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int totalPages;

    public PaginationResponse() {
    }

    public PaginationResponse(List<T> data, int page, int totalPages) {
        this.data = data;
        this.page = page;
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
