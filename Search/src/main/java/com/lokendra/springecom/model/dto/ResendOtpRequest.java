package com.lokendra.springecom.model.dto;

public class ResendOtpRequest {
    private String email;

    public ResendOtpRequest() {
    }

    public ResendOtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
