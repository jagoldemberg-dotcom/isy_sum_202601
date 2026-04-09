package com.duoc.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ShareRecipeRequest {

    @NotBlank(message = "La plataforma es obligatoria")
    @Size(max = 20, message = "La plataforma es demasiado larga")
    @Pattern(
            regexp = "^(sitio|facebook|whatsapp|x)$",
            message = "La plataforma indicada no está permitida"
    )
    private String platform;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
