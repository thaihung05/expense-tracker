package com.kei.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Gioi han so lan thu trong 1 khoang thoi gian (vd 10 lan dang nhap / phut / IP) de chong do mat khau.
 * Luu trong bo nho: du cho 1 server; neu chay nhieu server thi chuyen sang Redis.
 */
@Component
public class RateLimiter {

    private static final int MAX_KEYS = 50_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** true neu con duoc phep; false neu da vuot `limit` lan trong `period`. */
    public boolean tryAcquire(String key, int limit, Duration period) {
        long now = clock.millis();
        if (windows.size() > MAX_KEYS) {
            windows.values().removeIf(w -> now - w.startedAt >= period.toMillis());
        }
        Window w = windows.compute(key, (k, old) ->
                old == null || now - old.startedAt >= period.toMillis() ? new Window(now, 1) : old.next());
        return w.count <= limit;
    }

    private record Window(long startedAt, int count) {
        Window next() {
            return new Window(startedAt, count + 1);
        }
    }
}
