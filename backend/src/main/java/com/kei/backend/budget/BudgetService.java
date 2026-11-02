package com.kei.backend.budget;

import com.kei.backend.budget.BudgetDtos.BudgetRequest;
import com.kei.backend.budget.BudgetDtos.BudgetResponse;
import com.kei.backend.budget.BudgetDtos.Status;
import com.kei.backend.category.Category;
import com.kei.backend.category.CategoryResponse;
import com.kei.backend.category.CategoryService;
import com.kei.backend.category.CategoryType;
import com.kei.backend.common.ApiException;
import com.kei.backend.report.ReportService;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BudgetService {

    static final int WARNING_PERCENT = 80;

    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;
    private final ReportService reportService;

    public BudgetService(BudgetRepository budgetRepository, CategoryService categoryService,
            ReportService reportService) {
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
        this.reportService = reportService;
    }

    /** Danh sach ngan sach kem so da chi trong thang. */
    public List<BudgetResponse> list(Long userId, YearMonth month) {
        Map<Long, Long> spentByCategory = reportService.expenseByCategory(userId, month);
        return budgetRepository.findByUserIdOrderById(userId).stream()
                .map(b -> toResponse(b, spentByCategory.getOrDefault(b.getCategory().getId(), 0L)))
                .toList();
    }

    /** Tao moi, hoac doi han muc neu danh muc da co ngan sach. */
    @Transactional
    public BudgetResponse upsert(Long userId, BudgetRequest request, YearMonth month) {
        Category category = categoryService.findVisible(userId, request.categoryId());
        if (category.getType() != CategoryType.EXPENSE) {
            throw ApiException.badRequest("Chỉ đặt ngân sách cho danh mục chi");
        }
        Budget budget = budgetRepository.findByUserIdAndCategory_Id(userId, category.getId())
                .map(b -> {
                    b.changeLimit(request.limit());
                    return b;
                })
                .orElseGet(() -> budgetRepository.save(new Budget(userId, category, request.limit())));
        long spent = reportService.expenseByCategory(userId, month).getOrDefault(category.getId(), 0L);
        return toResponse(budget, spent);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy ngân sách " + id));
        budgetRepository.delete(budget);
    }

    static BudgetResponse toResponse(Budget budget, long spent) {
        long limit = budget.getAmountLimit();
        int percent = (int) Math.min(999, spent * 100 / limit);
        Status status = percent >= 100 ? Status.EXCEEDED : percent >= WARNING_PERCENT ? Status.WARNING : Status.OK;
        return new BudgetResponse(budget.getId(), CategoryResponse.from(budget.getCategory()),
                limit, spent, limit - spent, percent, status);
    }
}
