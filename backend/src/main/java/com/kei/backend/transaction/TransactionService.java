package com.kei.backend.transaction;

import com.kei.backend.category.Category;
import com.kei.backend.category.CategoryResponse;
import com.kei.backend.category.CategoryService;
import com.kei.backend.category.CategoryType;
import com.kei.backend.common.ApiException;
import com.kei.backend.common.PageResponse;
import com.kei.backend.quickentry.CategorySuggester;
import com.kei.backend.quickentry.KeywordLearner;
import com.kei.backend.quickentry.QuickEntryParser;
import com.kei.backend.quickentry.QuickEntryParser.ParsedEntry;
import com.kei.backend.transaction.TransactionDtos.ParseResponse;
import com.kei.backend.transaction.TransactionDtos.TransactionRequest;
import com.kei.backend.transaction.TransactionDtos.TransactionResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategorySuggester categorySuggester;
    private final KeywordLearner keywordLearner;
    private final Clock clock;
    private final QuickEntryParser parser = new QuickEntryParser();

    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService,
            CategorySuggester categorySuggester, KeywordLearner keywordLearner, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.categorySuggester = categorySuggester;
        this.keywordLearner = keywordLearner;
        this.clock = clock;
    }

    public PageResponse<TransactionResponse> list(Long userId, YearMonth month, Long categoryId,
            CategoryType type, int page, int size) {
        YearMonth m = month != null ? month : YearMonth.now(clock);
        var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        var result = transactionRepository.search(
                userId, m.atDay(1), m.atEndOfMonth(), categoryId, type, pageable);
        return PageResponse.of(result, TransactionResponse::from);
    }

    public TransactionResponse get(Long userId, Long id) {
        return TransactionResponse.from(findOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = categoryService.findVisible(userId, request.categoryId());
        String note = clean(request.note());
        Transaction saved = transactionRepository.save(
                new Transaction(userId, category, request.amount(), note, request.occurredOn()));
        if (Boolean.TRUE.equals(request.learnKeyword()) && note != null) {
            keywordLearner.learn(userId, note, category);
        }
        return TransactionResponse.from(saved);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = findOwned(userId, id);
        Category category = categoryService.findVisible(userId, request.categoryId());
        transaction.update(category, request.amount(), clean(request.note()), request.occurredOn());
        return TransactionResponse.from(transaction); // trong transaction: JPA tu luu thay doi (dirty checking)
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactionRepository.delete(findOwned(userId, id));
    }

    /** Doc cau go nhanh va doan danh muc. Chua luu gi ca, frontend se hien de nguoi dung xac nhan. */
    public ParseResponse parse(Long userId, String text) {
        ParsedEntry entry = parser.parse(text, LocalDate.now(clock));
        return categorySuggester.suggest(userId, entry.note())
                .map(s -> new ParseResponse(entry.amount(), entry.date(), entry.note(),
                        CategoryResponse.from(s.category()), s.keyword(), s.source()))
                .orElseGet(() -> new ParseResponse(entry.amount(), entry.date(), entry.note(), null, null, null));
    }

    public List<Transaction> findAllInMonth(Long userId, YearMonth month) {
        return transactionRepository.findAllInRange(userId, month.atDay(1), month.atEndOfMonth());
    }

    /** Chi tim giao dich cua chinh nguoi dung; cua nguoi khac thi bao 404 (khong tiet lo la co ton tai). */
    private Transaction findOwned(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy giao dịch " + id));
    }

    private static String clean(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        return note.trim();
    }
}
