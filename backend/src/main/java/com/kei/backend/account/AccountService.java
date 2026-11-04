package com.kei.backend.account;

import com.kei.backend.budget.BudgetRepository;
import com.kei.backend.category.CategoryRepository;
import com.kei.backend.category.CategoryType;
import com.kei.backend.common.ApiException;
import com.kei.backend.quickentry.KeywordRuleRepository;
import com.kei.backend.transaction.TransactionRepository;
import com.kei.backend.user.User;
import com.kei.backend.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quyen cua chu the du lieu (Luat 91/2025/QH15, Dieu 4): xem / tai du lieu cua minh,
 * xoa du lieu va rut lai su dong y (xoa tai khoan).
 */
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final KeywordRuleRepository keywordRuleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, CategoryRepository categoryRepository,
            TransactionRepository transactionRepository, BudgetRepository budgetRepository,
            KeywordRuleRepository keywordRuleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.keywordRuleRepository = keywordRuleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Toan bo du lieu app dang luu ve nguoi dung nay (khong gom mat khau). */
    @Transactional(readOnly = true)
    public DataExport export(Long userId) {
        User u = findUser(userId);
        return new DataExport(
                Instant.now(),
                new DataExport.Account(u.getEmail(), u.getDisplayName(), u.getCreatedAt(),
                        u.getPrivacyAcceptedAt(), u.getPrivacyVersion()),
                categoryRepository.findByUserIdOrderById(userId).stream()
                        .map(c -> new DataExport.Category(c.getName(), c.getType(), c.getIcon()))
                        .toList(),
                transactionRepository.findByUserIdOrderByOccurredOnAscIdAsc(userId).stream()
                        .map(t -> new DataExport.Transaction(t.getOccurredOn(), t.getCategory().getType(),
                                t.getCategory().getName(), t.getAmount(), t.getNote(), t.getCreatedAt()))
                        .toList(),
                budgetRepository.findByUserIdOrderById(userId).stream()
                        .map(b -> new DataExport.Budget(b.getCategory().getName(), b.getAmountLimit()))
                        .toList(),
                keywordRuleRepository.findByUserId(userId).stream()
                        .map(r -> new DataExport.LearnedKeyword(r.getKeyword(), r.getCategory().getName(), r.getHits()))
                        .toList());
    }

    /**
     * Xoa vinh vien tai khoan va MOI du lieu lien quan (giao dich, danh muc rieng, ngan sach, tu khoa da hoc):
     * database xoa day chuyen nho ON DELETE CASCADE. Phai nhap lai mat khau de tranh bi xoa nham / bi lay token.
     */
    @Transactional
    public void delete(Long userId, String password) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiException.badRequest("Mật khẩu không đúng");
        }
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Tài khoản không còn tồn tại"));
    }

    public record DataExport(
            Instant exportedAt, Account account, List<Category> customCategories, List<Transaction> transactions,
            List<Budget> budgets, List<LearnedKeyword> learnedKeywords) {

        public record Account(String email, String displayName, Instant createdAt,
                Instant privacyAcceptedAt, String privacyVersion) {
        }

        public record Category(String name, CategoryType type, String icon) {
        }

        public record Transaction(LocalDate date, CategoryType type, String category, long amount, String note,
                Instant createdAt) {
        }

        public record Budget(String category, long monthlyLimit) {
        }

        public record LearnedKeyword(String keyword, String category, int hits) {
        }
    }
}
