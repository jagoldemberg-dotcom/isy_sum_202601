package com.duoc.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddPhotoRequest {

    @NotBlank(message = "La URL de la foto es obligatoria")
    @Size(max = 500, message = "La URL de la foto supera el largo permitido")
    @Pattern(
            regexp = "^(https://)([\\w.-]+)(:[0-9]+)?(/.*)?$",
            message = "La URL de la foto debe comenzar con https://"
    )
    private String photoUrl;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
