package com.kei.backend.security;

import com.kei.backend.common.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Gioi han so lan goi cac API nhay cam (dang nhap, dang ky, xoa tai khoan) tren moi dia chi IP. */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED = Set.of("/api/auth/login", "/api/auth/register", "/api/account/delete");

    private final RateLimiter rateLimiter;
    private final AppProperties props;

    public AuthRateLimitFilter(RateLimiter rateLimiter, AppProperties props) {
        this.rateLimiter = rateLimiter;
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !PROTECTED.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = "ip:" + request.getRemoteAddr() + ":" + request.getRequestURI();
        if (rateLimiter.tryAcquire(key, props.rateLimit().authPerMinute(), Duration.ofMinutes(1))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":429,\"title\":\"Too Many Requests\","
                + "\"detail\":\"Bạn thử quá nhiều lần, vui lòng đợi 1 phút rồi thử lại.\"}");
    }
}
