package com.kei.backend.transaction;

import com.kei.backend.category.CategoryType;
import com.kei.backend.common.PageResponse;
import com.kei.backend.security.CurrentUserId;
import com.kei.backend.transaction.TransactionDtos.ParseRequest;
import com.kei.backend.transaction.TransactionDtos.ParseResponse;
import com.kei.backend.transaction.TransactionDtos.TransactionRequest;
import com.kei.backend.transaction.TransactionDtos.TransactionResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final Clock clock;

    public TransactionController(TransactionService transactionService, Clock clock) {
        this.transactionService = transactionService;
        this.clock = clock;
    }

    /** Vd: GET /api/transactions?month=2026-11&type=EXPENSE&page=0&size=20 */
    @GetMapping
    public PageResponse<TransactionResponse> list(@CurrentUserId Long userId,
            @RequestParam(required = false) YearMonth month,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return transactionService.list(userId, month, categoryId, type, page, size);
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@CurrentUserId Long userId, @PathVariable Long id) {
        return transactionService.get(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@CurrentUserId Long userId, @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(userId, request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@CurrentUserId Long userId, @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long id) {
        transactionService.delete(userId, id);
    }

    /** Doc cau go nhanh, vd {"text": "cafe 35k hom qua"}. */
    @PostMapping("/parse")
    public ParseResponse parse(@CurrentUserId Long userId, @Valid @RequestBody ParseRequest request) {
        return transactionService.parse(userId, request.text());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@CurrentUserId Long userId,
            @RequestParam(required = false) YearMonth month) {
        YearMonth m = month != null ? month : YearMonth.now(clock);
        String csv = CsvExporter.toCsv(transactionService.findAllInMonth(userId, m));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("thu-chi-" + m + ".csv").build().toString())
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
