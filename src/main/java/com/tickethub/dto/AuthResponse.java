package com.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long   id;
    private String name;
    private String email;
    private String role;

    public java.util.Map<String, Object> getUser() {
        return java.util.Map.of("id", id, "name", name, "email", email, "role", role);
    }
}
