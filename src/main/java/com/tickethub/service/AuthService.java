package com.tickethub.service;

import com.tickethub.dto.*;
import com.tickethub.model.User;
import com.tickethub.repository.UserRepository;
import com.tickethub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil         jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail().toLowerCase()))
            throw new RuntimeException("An account with this email already exists.");

        User user = User.builder()
            .name(req.getName().trim())
            .email(req.getEmail().toLowerCase().trim())
            .phone(req.getPhone())
            .password(encoder.encode(req.getPassword()))
            .role(User.Role.customer)
            .build();

        userRepo.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new RuntimeException("No account found with that email."));

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Incorrect password.");

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
        user.setName(req.getName().trim());
        user.setPhone(req.getPhone());
        userRepo.save(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));

        if (!encoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new RuntimeException("Current password is incorrect.");

        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }
}
