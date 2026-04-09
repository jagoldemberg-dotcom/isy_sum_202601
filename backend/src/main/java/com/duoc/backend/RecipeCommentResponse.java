package com.duoc.backend;

import java.time.LocalDateTime;

public class RecipeCommentResponse {

    private final Long id;
    private final String username;
    private final String commentText;
    private final Integer rating;
    private final LocalDateTime createdAt;

    public RecipeCommentResponse(Long id, String username, String commentText, Integer rating, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.commentText = commentText;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static RecipeCommentResponse fromEntity(RecipeComment comment) {
        return new RecipeCommentResponse(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getCommentText(),
                comment.getRating(),
                comment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getCommentText() {
        return commentText;
    }

    public Integer getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
