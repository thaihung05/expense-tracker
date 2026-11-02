package com.kei.backend.auth;

import com.kei.backend.auth.AuthDtos.AuthResponse;
import com.kei.backend.auth.AuthDtos.LoginRequest;
import com.kei.backend.auth.AuthDtos.RegisterRequest;
import com.kei.backend.auth.AuthDtos.UserResponse;
import com.kei.backend.common.ApiException;
import com.kei.backend.security.TokenService;
import com.kei.backend.user.User;
import com.kei.backend.user.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email này đã được đăng ký");
        }
        User user = userRepository.save(new User(
                email, passwordEncoder.encode(request.password()), request.displayName().trim()));
        return new AuthResponse(tokenService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Sai email hay sai mat khau deu bao chung 1 cau, de nguoi ngoai khong do duoc email nao co tai khoan.
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Email hoặc mật khẩu không đúng"));
        return new AuthResponse(tokenService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn tồn tại"));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
