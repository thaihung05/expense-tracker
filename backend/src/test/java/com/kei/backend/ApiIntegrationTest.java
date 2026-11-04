package com.kei.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Test tich hop: goi API qua tang HTTP, chay voi PostgreSQL that (Testcontainers).
 * "Hom nay" duoc co dinh la 15/11/2026 de ket qua test khong phu thuoc ngay chay.
 */
@SpringBootTest(properties = {
    // test goi dang ky/dang nhap lien tuc: noi rong gioi han (gioi han duoc test rieng trong RateLimitIntegrationTest)
    "app.rate-limit.auth-per-minute=100000",
    "app.rate-limit.login-per-email=100000"
})
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, ApiIntegrationTest.FixedClock.class})
class ApiIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-15T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }

    @Autowired
    private MockMvc mvc;

    private String token;

    @BeforeEach
    void registerFreshUser() throws Exception {
        token = register("user-" + UUID.randomUUID() + "@test.vn");
    }

    // ---------- Dang ky / dang nhap ----------

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/transactions").header("Authorization", "Bearer khong-hop-le"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndMe() throws Exception {
        String email = "Login-" + UUID.randomUUID() + "@Test.vn";
        register(email);

        String body = mvc.perform(json(post("/api/auth/login"),
                        Map.of("email", email.toLowerCase(), "password", "matkhau123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String loginToken = JsonPath.read(body, "$.token");

        mvc.perform(auth(get("/api/auth/me"), loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()));
    }

    @Test
    void duplicateEmailAndWrongPasswordAreRejected() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@test.vn";
        register(email);

        mvc.perform(json(post("/api/auth/register"),
                        Map.of("email", email, "password", "matkhau123", "displayName", "B", "acceptedPrivacy", true)))
                .andExpect(status().isConflict());
        mvc.perform(json(post("/api/auth/login"), Map.of("email", email, "password", "sai-mat-khau")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mvc.perform(json(post("/api/auth/register"), Map.of("email", "khong-phai-email", "password", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.displayName").exists())
                .andExpect(jsonPath("$.errors.acceptedPrivacy").exists());
    }

    // ---------- Quyen rieng tu (Luat 91/2025/QH15) ----------

    @Test
    void registrationRequiresExplicitConsent() throws Exception {
        String email = "consent-" + UUID.randomUUID() + "@test.vn";
        mvc.perform(json(post("/api/auth/register"),
                        Map.of("email", email, "password", "matkhau123", "displayName", "A", "acceptedPrivacy", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.acceptedPrivacy").exists());
        mvc.perform(json(post("/api/auth/register"),
                        Map.of("email", email, "password", "matkhau123", "displayName", "A")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.acceptedPrivacy").exists());
    }

    @Test
    void publicConfigNeedsNoLogin() throws Exception {
        mvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationEnabled").value(true))
                .andExpect(jsonPath("$.privacyVersion").isNotEmpty());
    }

    @Test
    void exportContainsAllMyDataButNoPassword() throws Exception {
        createTransaction(35_000, categoryId("Ăn uống"), "2026-11-14", "cafe", true);

        String body = mvc.perform(auth(get("/api/account/export")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"du-lieu-cua-toi.json\""))
                .andExpect(jsonPath("$.account.privacyVersion").isNotEmpty())
                .andExpect(jsonPath("$.account.privacyAcceptedAt").isNotEmpty())
                .andExpect(jsonPath("$.transactions[0].note").value("cafe"))
                .andExpect(jsonPath("$.learnedKeywords[0].keyword").value("cafe"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(body).doesNotContainIgnoringCase("password\"").doesNotContain("{bcrypt}");
    }

    @Test
    void deleteAccountRemovesEverything() throws Exception {
        String email = "delete-" + UUID.randomUUID() + "@test.vn";
        String myToken = register(email);
        long food = categoryIdFor("Ăn uống", myToken);
        mvc.perform(json(auth(post("/api/transactions"), myToken),
                Map.of("amount", 35_000, "categoryId", food, "occurredOn", "2026-11-14", "note", "cafe", "learnKeyword", true)));
        mvc.perform(json(auth(put("/api/budgets"), myToken), Map.of("categoryId", food, "limit", 100_000)));
        mvc.perform(json(auth(post("/api/categories"), myToken), Map.of("name", "Riêng", "type", "EXPENSE")));

        mvc.perform(json(auth(post("/api/account/delete"), myToken), Map.of("password", "sai-mat-khau")))
                .andExpect(status().isBadRequest());
        mvc.perform(json(auth(post("/api/account/delete"), myToken), Map.of("password", "matkhau123")))
                .andExpect(status().isNoContent());

        mvc.perform(auth(get("/api/auth/me"), myToken)).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/api/auth/login"), Map.of("email", email, "password", "matkhau123")))
                .andExpect(status().isUnauthorized());
        // Email duoc giai phong: dang ky lai tu dau duoc
        register(email);
    }

    // ---------- Giao dich ----------

    @Test
    void createListUpdateDeleteTransaction() throws Exception {
        long food = categoryId("Ăn uống");
        long id = createTransaction(35_000, food, "2026-11-14", "cafe", false);

        mvc.perform(auth(get("/api/transactions?month=2026-11")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].note").value("cafe"))
                .andExpect(jsonPath("$.items[0].type").value("EXPENSE"));

        mvc.perform(json(auth(put("/api/transactions/" + id)),
                        Map.of("amount", 40_000, "categoryId", food, "occurredOn", "2026-11-14", "note", "cafe sữa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(40_000));

        mvc.perform(auth(delete("/api/transactions/" + id))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/transactions/" + id))).andExpect(status().isNotFound());
    }

    @Test
    void listCanFilterByMonthAndType() throws Exception {
        createTransaction(35_000, categoryId("Ăn uống"), "2026-11-14", "cafe", false);
        createTransaction(15_000_000, categoryId("Lương"), "2026-11-01", "lương", false);
        createTransaction(50_000, categoryId("Ăn uống"), "2026-10-31", "phở", false);

        mvc.perform(auth(get("/api/transactions?month=2026-11&type=INCOME")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].amount").value(15_000_000));
        mvc.perform(auth(get("/api/transactions?month=2026-10")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void invalidTransactionIsRejected() throws Exception {
        mvc.perform(json(auth(post("/api/transactions")), Map.of("amount", -5, "occurredOn", "2026-11-14")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        long id = createTransaction(35_000, categoryId("Ăn uống"), "2026-11-14", "cafe", false);
        String otherToken = register("other-" + UUID.randomUUID() + "@test.vn");

        mvc.perform(auth(get("/api/transactions/" + id), otherToken)).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/transactions/" + id), otherToken)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/transactions?month=2026-11"), otherToken))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    // ---------- Nhap cau tu nhien + tu hoc ----------

    @Test
    void parseUnderstandsAmountDateAndCategory() throws Exception {
        mvc.perform(json(auth(post("/api/transactions/parse")), Map.of("text", "cafe 35k hôm qua")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(35_000))
                .andExpect(jsonPath("$.occurredOn").value("2026-11-14"))
                .andExpect(jsonPath("$.note").value("cafe"))
                .andExpect(jsonPath("$.category.name").value("Ăn uống"))
                .andExpect(jsonPath("$.source").value("DICTIONARY"));
    }

    @Test
    void appLearnsNewKeywordsFromSavedTransactions() throws Exception {
        mvc.perform(json(auth(post("/api/transactions/parse")), Map.of("text", "gửi tiền mẹ 500k")))
                .andExpect(jsonPath("$.category").isEmpty());

        createTransaction(500_000, categoryId("Khác"), "2026-11-15", "gửi tiền mẹ", true);

        mvc.perform(json(auth(post("/api/transactions/parse")), Map.of("text", "gửi tiền mẹ 300k")))
                .andExpect(jsonPath("$.category.name").value("Khác"))
                .andExpect(jsonPath("$.source").value("LEARNED"));
    }

    // ---------- Bao cao + ngan sach ----------

    @Test
    void monthlySummaryAddsUpIncomeAndExpense() throws Exception {
        createTransaction(35_000, categoryId("Ăn uống"), "2026-11-14", "cafe", false);
        createTransaction(65_000, categoryId("Ăn uống"), "2026-11-15", "lẩu", false);
        createTransaction(15_000_000, categoryId("Lương"), "2026-11-01", "lương", false);

        mvc.perform(auth(get("/api/reports/summary?month=2026-11")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(15_000_000))
                .andExpect(jsonPath("$.totalExpense").value(100_000))
                .andExpect(jsonPath("$.balance").value(14_900_000))
                .andExpect(jsonPath("$.expenseByCategory[0].name").value("Ăn uống"))
                .andExpect(jsonPath("$.daily", hasSize(30)))
                .andExpect(jsonPath("$.daily[13].expense").value(35_000));
    }

    @Test
    void budgetShowsWarningAndExceeded() throws Exception {
        long food = categoryId("Ăn uống");
        mvc.perform(json(auth(put("/api/budgets")), Map.of("categoryId", food, "limit", 100_000)))
                .andExpect(jsonPath("$.status").value("OK"));

        createTransaction(85_000, food, "2026-11-10", "đi chợ", false);
        mvc.perform(auth(get("/api/budgets")))
                .andExpect(jsonPath("$[0].percent").value(85))
                .andExpect(jsonPath("$[0].status").value("WARNING"));

        createTransaction(30_000, food, "2026-11-11", "cafe", false);
        mvc.perform(auth(get("/api/budgets")))
                .andExpect(jsonPath("$[0].remaining").value(-15_000))
                .andExpect(jsonPath("$[0].status").value("EXCEEDED"));
    }

    @Test
    void budgetOnlyForExpenseCategories() throws Exception {
        mvc.perform(json(auth(put("/api/budgets")), Map.of("categoryId", categoryId("Lương"), "limit", 1_000)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportMonthAsCsv() throws Exception {
        createTransaction(35_000, categoryId("Ăn uống"), "2026-11-14", "cafe, sữa", false);

        String csv = mvc.perform(auth(get("/api/transactions/export?month=2026-11")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"thu-chi-2026-11.csv\""))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("2026-11-14,Chi,Ăn uống,35000,\"cafe, sữa\"");
    }

    // ---------- Danh muc ----------

    @Test
    void customCategoriesBelongToTheirOwner() throws Exception {
        mvc.perform(json(auth(post("/api/categories")), Map.of("name", "Thú cưng", "type", "EXPENSE", "icon", "🐶")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault").value(false));
        mvc.perform(json(auth(post("/api/categories")), Map.of("name", "thu cung", "type", "EXPENSE")))
                .andExpect(status().isConflict());

        String otherToken = register("cat-" + UUID.randomUUID() + "@test.vn");
        String names = mvc.perform(auth(get("/api/categories"), otherToken))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(names).doesNotContain("Thú cưng");
    }

    @Test
    void wrongEnumValueIsBadRequest() throws Exception {
        mvc.perform(auth(get("/api/categories?type=ABC")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // ---------- Ham phu tro ----------

    private String register(String email) throws Exception {
        String body = mvc.perform(json(post("/api/auth/register"),
                        Map.of("email", email, "password", "matkhau123", "displayName", "Test", "acceptedPrivacy", true)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long categoryId(String name) throws Exception {
        return categoryIdFor(name, token);
    }

    private long categoryIdFor(String name, String jwt) throws Exception {
        String body = mvc.perform(auth(get("/api/categories"), jwt))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        List<Integer> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst();
    }

    private long createTransaction(long amount, long categoryId, String date, String note, boolean learn)
            throws Exception {
        String body = mvc.perform(json(auth(post("/api/transactions")), Map.of(
                        "amount", amount, "categoryId", categoryId, "occurredOn", date,
                        "note", note, "learnKeyword", learn)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return auth(request, token);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String jwt) {
        return request.header("Authorization", "Bearer " + jwt);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Map<String, ?> body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(toJson(body));
    }

    /** JSON don gian cho Map phang (du dung trong test, khong can them thu vien). */
    private static String toJson(Map<String, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        map.forEach((k, v) -> {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append('"').append(k).append("\":");
            if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append('"').append(v.toString().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        });
        return sb.append('}').toString();
    }
}
