package com.rentalapp.auth;

import com.rentalapp.auth.dto.AuthResponse;
import com.rentalapp.auth.dto.LoginRequest;
import com.rentalapp.auth.dto.RefreshRequest;
import com.rentalapp.auth.dto.RegisterRequest;
import com.rentalapp.common.exception.BadRequestException;
import com.rentalapp.user.Role;
import com.rentalapp.user.User;
import com.rentalapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;


 // registration of new user   

// Input: RegisterRequest
// Output: AuthResponse

    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(request.role())
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtService.extractUsername(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new BadRequestException("Refresh token expired or invalid");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                accessToken, refreshToken);
    }
}
