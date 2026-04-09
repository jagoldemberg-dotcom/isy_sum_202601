package com.duoc.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddVideoRequest {

    @NotBlank(message = "La URL del video es obligatoria")
    @Size(max = 500, message = "La URL del video supera el largo permitido")
    @Pattern(
            regexp = "^(https://)(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com)(/.*)?$",
            message = "La URL del video debe corresponder a un proveedor permitido"
    )
    private String videoUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
