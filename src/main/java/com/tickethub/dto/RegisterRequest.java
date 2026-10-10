package com.tickethub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    private String phone;
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
