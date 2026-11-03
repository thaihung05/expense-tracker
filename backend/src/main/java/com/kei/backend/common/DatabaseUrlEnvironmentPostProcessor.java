package com.kei.backend.common;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Cho phep cau hinh database bang 1 chuoi kieu Neon / Render / Heroku:
 * {@code DATABASE_URL=postgresql://user:pass@host/db?sslmode=require}
 * thay vi 3 bien DB_URL, DB_USERNAME, DB_PASSWORD.
 *
 * <p>Uu tien: DB_URL (neu co) > DATABASE_URL_UNPOOLED > DATABASE_URL.
 * Neu chuoi tro toi cong pooler cua Neon ("-pooler"), doi sang ket noi truc tiep:
 * app chay lien tuc va da co pool rieng (HikariCP), con Flyway can ket noi truc tiep de chay migration.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        if (hasText(env.getProperty("DB_URL"))) {
            return;
        }
        String raw = hasText(env.getProperty("DATABASE_URL_UNPOOLED"))
                ? env.getProperty("DATABASE_URL_UNPOOLED")
                : env.getProperty("DATABASE_URL");
        if (!hasText(raw)) {
            return;
        }
        env.getPropertySources().addFirst(new MapPropertySource("databaseUrl", toDatasourceProperties(raw)));
    }

    static Map<String, Object> toDatasourceProperties(String raw) {
        URI uri = URI.create(raw.trim());
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL phai bat dau bang postgresql:// hoac postgres://");
        }

        String host = uri.getHost().replace("-pooler.", ".");
        String port = uri.getPort() > 0 ? ":" + uri.getPort() : "";
        String query = uri.getRawQuery() == null ? "" : Arrays.stream(uri.getRawQuery().split("&"))
                .filter(p -> !p.isBlank() && !p.startsWith("channel_binding=")) // tham so cua libpq, JDBC khong dung
                .collect(Collectors.joining("&"));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("spring.datasource.url",
                "jdbc:postgresql://" + host + port + uri.getRawPath() + (query.isEmpty() ? "" : "?" + query));

        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            props.put("spring.datasource.username", decode(colon >= 0 ? userInfo.substring(0, colon) : userInfo));
            if (colon >= 0) {
                props.put("spring.datasource.password", decode(userInfo.substring(colon + 1)));
            }
        }
        return props;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
