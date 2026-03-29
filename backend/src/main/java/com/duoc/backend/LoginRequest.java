package com.duoc.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 80, message = "El usuario excede el largo permitido")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 100, message = "La contraseña excede el largo permitido")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
