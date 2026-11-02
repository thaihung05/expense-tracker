package com.kei.backend.report;

import java.time.LocalDate;
import java.util.List;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record DailyPoint(LocalDate date, long income, long expense) {
    }

    /** Tong ket 1 thang: tong thu, tong chi, so du, chi tiet theo danh muc va theo ngay. */
    public record MonthlySummary(
            String month, long totalIncome, long totalExpense, long balance,
            List<CategoryTotal> expenseByCategory, List<CategoryTotal> incomeByCategory,
            List<DailyPoint> daily) {
    }
}
