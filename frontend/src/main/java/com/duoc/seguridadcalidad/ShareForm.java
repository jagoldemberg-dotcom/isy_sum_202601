package com.duoc.seguridadcalidad;

public class ShareForm {

    private String platform = "sitio";

    public String getPlatform() {
        return platform == null || platform.isBlank() ? "sitio" : platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
