package com.kei.backend.auth;

import com.kei.backend.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cac object vao/ra cua API dang ky, dang nhap. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không hợp lệ") @Size(max = 255) String email,
            @NotBlank(message = "Vui lòng nhập mật khẩu") @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự") String password,
            @NotBlank(message = "Vui lòng nhập tên") @Size(max = 100) String displayName) {
    }

    public record LoginRequest(
            @NotBlank(message = "Vui lòng nhập email") String email,
            @NotBlank(message = "Vui lòng nhập mật khẩu") String password) {
    }

    public record UserResponse(Long id, String email, String displayName) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
        }
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
