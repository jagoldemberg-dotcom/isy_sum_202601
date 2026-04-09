package com.duoc.backend;

public class RecipeShareResponse {

    private final String message;
    private final String platform;
    private final String shareUrl;

    public RecipeShareResponse(String message, String platform, String shareUrl) {
        this.message = message;
        this.platform = platform;
        this.shareUrl = shareUrl;
    }

    public String getMessage() {
        return message;
    }

    public String getPlatform() {
        return platform;
    }

    public String getShareUrl() {
        return shareUrl;
    }
}
