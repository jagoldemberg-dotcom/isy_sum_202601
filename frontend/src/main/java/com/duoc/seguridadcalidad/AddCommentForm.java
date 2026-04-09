package com.duoc.seguridadcalidad;

public class AddCommentForm {

    private String commentText;
    private Integer rating;

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Integer getRating() {
        return rating == null ? 5 : rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
