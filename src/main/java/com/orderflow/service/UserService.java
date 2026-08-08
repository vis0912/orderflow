package com.orderflow.service;

import com.orderflow.dto.LoginRequest;
import com.orderflow.dto.LoginResponse;
import com.orderflow.dto.RegisterRequest;
import com.orderflow.dto.UserResponse;
import com.orderflow.entity.Role;
import com.orderflow.entity.User;
import com.orderflow.exception.EmailAlreadyExistsException;
import com.orderflow.exception.InvalidCredentialsException;
import com.orderflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getActive()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token, "Bearer");
    }
}