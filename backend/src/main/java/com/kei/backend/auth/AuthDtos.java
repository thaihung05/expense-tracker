package com.kei.backend.auth;

import com.kei.backend.user.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cac object vao/ra cua API dang ky, dang nhap. */
public final class AuthDtos {

    private AuthDtos() {
    }

    /**
     * acceptedPrivacy: nguoi dung phai tu tick o "Toi dong y..." (khong tick san).
     * Luat 91/2025/QH15 Dieu 9: im lang / khong phan hoi khong duoc coi la dong y.
     */
    public record RegisterRequest(
            @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không hợp lệ") @Size(max = 255) String email,
            @NotBlank(message = "Vui lòng nhập mật khẩu") @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự") String password,
            @NotBlank(message = "Vui lòng nhập tên") @Size(max = 100) String displayName,
            @NotNull(message = "Bạn cần đồng ý với Chính sách quyền riêng tư để tạo tài khoản")
            @AssertTrue(message = "Bạn cần đồng ý với Chính sách quyền riêng tư để tạo tài khoản") Boolean acceptedPrivacy) {
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

    /** Thong tin cong khai cho frontend: co cho dang ky khong, chinh sach dang o phien ban nao. */
    public record PublicConfig(boolean registrationEnabled, String privacyVersion) {
    }
}
