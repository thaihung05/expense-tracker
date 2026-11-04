package com.kei.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Chong do mat khau: gioi han theo IP va theo email. Moi test dung 1 IP rieng de khong anh huong nhau. */
@SpringBootTest(properties = {"app.rate-limit.auth-per-minute=5", "app.rate-limit.login-per-email=3"})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void tooManyLoginAttemptsFromOneIpAreBlocked() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(login("10.0.0.1", "ip-" + i + "@test.vn")).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("10.0.0.1", "ip-6@test.vn"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
        // IP khac khong bi anh huong
        mvc.perform(login("10.0.0.2", "ip-7@test.vn")).andExpect(status().isUnauthorized());
    }

    @Test
    void tooManyAttemptsOnOneEmailAreBlockedEvenFromDifferentIps() throws Exception {
        String email = "target-" + UUID.randomUUID() + "@test.vn";
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("10.1.0." + i, email)).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("10.1.0.99", email)).andExpect(status().isTooManyRequests());
    }

    private static MockHttpServletRequestBuilder login(String ip, String email) {
        return post("/api/auth/login")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"sai-mat-khau\"}");
    }
}
