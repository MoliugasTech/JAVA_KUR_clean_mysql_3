package com.example.fooddeliverymobile;

public class ReviewInfo {
    private Long id;
    private String type;
    private int rating;
    private String comment;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }
}
