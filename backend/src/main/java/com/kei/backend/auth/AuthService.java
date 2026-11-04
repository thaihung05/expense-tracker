package com.kei.backend.auth;

import com.kei.backend.auth.AuthDtos.AuthResponse;
import com.kei.backend.auth.AuthDtos.LoginRequest;
import com.kei.backend.auth.AuthDtos.PublicConfig;
import com.kei.backend.auth.AuthDtos.RegisterRequest;
import com.kei.backend.auth.AuthDtos.UserResponse;
import com.kei.backend.common.ApiException;
import com.kei.backend.common.AppProperties;
import com.kei.backend.common.PrivacyPolicy;
import com.kei.backend.security.RateLimiter;
import com.kei.backend.security.TokenService;
import com.kei.backend.user.User;
import com.kei.backend.user.UserRepository;
import java.time.Duration;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RateLimiter rateLimiter;
    private final AppProperties props;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
            RateLimiter rateLimiter, AppProperties props) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.rateLimiter = rateLimiter;
        this.props = props;
    }

    public PublicConfig publicConfig() {
        return new PublicConfig(props.registrationEnabled(), PrivacyPolicy.VERSION);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!props.registrationEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ứng dụng đang tạm đóng đăng ký tài khoản mới");
        }
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email này đã được đăng ký");
        }
        User user = userRepository.save(new User(
                email, passwordEncoder.encode(request.password()), request.displayName().trim(), PrivacyPolicy.VERSION));
        return new AuthResponse(tokenService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Chong do mat khau 1 tai khoan cu the, ke ca khi ke tan cong doi IP lien tuc.
        if (!rateLimiter.tryAcquire("login-email:" + email, props.rateLimit().loginPerEmail(), Duration.ofMinutes(15))) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Tài khoản này bị thử đăng nhập quá nhiều lần, vui lòng đợi 15 phút.");
        }
        // Sai email hay sai mat khau deu bao chung 1 cau, de nguoi ngoai khong do duoc email nao co tai khoan.
        User user = userRepository.findByEmail(email)
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

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
