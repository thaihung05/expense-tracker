package com.kei.backend.common;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cac cau hinh "app.*" trong application.properties, doc vao 1 object co kieu ro rang. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, ZoneId zone, Cors cors, RateLimit rateLimit, boolean registrationEnabled) {

    public record Jwt(String secret, Duration expiration) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** authPerMinute: so lan goi dang nhap/dang ky moi phut moi IP; loginPerEmail: so lan sai moi email / 15 phut. */
    public record RateLimit(int authPerMinute, int loginPerEmail) {
    }
}
