package com.tickethub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    @com.fasterxml.jackson.annotation.JsonAlias({"currentPassword", "current_password"})
    private String currentPassword;
    @NotBlank @Size(min = 6)
    @com.fasterxml.jackson.annotation.JsonAlias({"newPassword", "new_password"})
    private String newPassword;
}
