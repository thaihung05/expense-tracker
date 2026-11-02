package com.kei.backend.budget;

import com.kei.backend.budget.BudgetDtos.BudgetRequest;
import com.kei.backend.budget.BudgetDtos.BudgetResponse;
import com.kei.backend.security.CurrentUserId;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final Clock clock;

    public BudgetController(BudgetService budgetService, Clock clock) {
        this.budgetService = budgetService;
        this.clock = clock;
    }

    @GetMapping
    public List<BudgetResponse> list(@CurrentUserId Long userId, @RequestParam(required = false) YearMonth month) {
        return budgetService.list(userId, month != null ? month : YearMonth.now(clock));
    }

    /** PUT vi thao tac nay "dat" han muc: goi nhieu lan voi cung du lieu thi ket qua nhu nhau. */
    @PutMapping
    public BudgetResponse upsert(@CurrentUserId Long userId, @Valid @RequestBody BudgetRequest request) {
        return budgetService.upsert(userId, request, YearMonth.now(clock));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long id) {
        budgetService.delete(userId, id);
    }
}
