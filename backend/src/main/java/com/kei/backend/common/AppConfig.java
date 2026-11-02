package com.kei.backend.common;

import java.time.Clock;
import java.time.YearMonth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    /**
     * Dong ho theo mui gio nguoi dung. Moi cho can "hom nay" deu lay tu day,
     * nen khi test co the thay bang dong ho co dinh.
     */
    @Bean
    Clock clock(AppProperties props) {
        return Clock.system(props.zone());
    }

    /** Cho phep nhan tham so ?month=2026-11 thanh YearMonth. */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, YearMonth.class, YearMonth::parse);
    }
}
