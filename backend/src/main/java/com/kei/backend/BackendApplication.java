package com.kei.backend;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

    public static void main(String[] args) {
        // Server luon chay gio UTC; "hom nay" cua nguoi dung tinh theo app.zone (xem AppConfig).
        // (Tren Windows, Java bao mui gio "Asia/Saigon" ma PostgreSQL khong nhan.)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BackendApplication.class, args);
    }

}
