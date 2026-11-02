package com.kei.backend.report;

import com.kei.backend.category.CategoryType;
import com.kei.backend.report.ReportDtos.DailyPoint;
import com.kei.backend.report.ReportDtos.MonthlySummary;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public MonthlySummary summary(Long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<CategoryTotal> byCategory = reportRepository.totalsByCategory(userId, from, to);
        List<CategoryTotal> expense = byCategory.stream().filter(c -> c.type() == CategoryType.EXPENSE).toList();
        List<CategoryTotal> income = byCategory.stream().filter(c -> c.type() == CategoryType.INCOME).toList();
        long totalExpense = expense.stream().mapToLong(CategoryTotal::total).sum();
        long totalIncome = income.stream().mapToLong(CategoryTotal::total).sum();

        return new MonthlySummary(month.toString(), totalIncome, totalExpense, totalIncome - totalExpense,
                expense, income, daily(userId, from, to));
    }

    /** Tong chi theo tung danh muc trong thang (dung cho ngan sach). */
    public Map<Long, Long> expenseByCategory(Long userId, YearMonth month) {
        return reportRepository.totalsByCategory(userId, month.atDay(1), month.atEndOfMonth()).stream()
                .filter(c -> c.type() == CategoryType.EXPENSE)
                .collect(Collectors.toMap(CategoryTotal::categoryId, CategoryTotal::total));
    }

    /** Du lieu cho bieu do cot: moi ngay trong thang 1 diem, ngay khong co giao dich thi = 0. */
    private List<DailyPoint> daily(Long userId, LocalDate from, LocalDate to) {
        Map<LocalDate, Map<CategoryType, Long>> totals = reportRepository.totalsByDay(userId, from, to).stream()
                .collect(Collectors.groupingBy(DailyTypeTotal::date,
                        Collectors.toMap(DailyTypeTotal::type, DailyTypeTotal::total)));

        List<DailyPoint> points = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            Map<CategoryType, Long> day = totals.getOrDefault(d, Map.of());
            points.add(new DailyPoint(d,
                    day.getOrDefault(CategoryType.INCOME, 0L),
                    day.getOrDefault(CategoryType.EXPENSE, 0L)));
        }
        return points;
    }
}
