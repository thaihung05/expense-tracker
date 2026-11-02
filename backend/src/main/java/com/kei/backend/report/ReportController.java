package com.kei.backend.report;

import com.kei.backend.report.ReportDtos.MonthlySummary;
import com.kei.backend.security.CurrentUserId;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final Clock clock;

    public ReportController(ReportService reportService, Clock clock) {
        this.reportService = reportService;
        this.clock = clock;
    }

    /** Vd: GET /api/reports/summary?month=2026-11 (khong truyen month = thang nay). */
    @GetMapping("/summary")
    public MonthlySummary summary(@CurrentUserId Long userId, @RequestParam(required = false) YearMonth month) {
        return reportService.summary(userId, month != null ? month : YearMonth.now(clock));
    }
}
