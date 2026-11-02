package com.kei.backend.report;

import com.kei.backend.transaction.Transaction;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/** Cac cau truy van tong hop (SUM, GROUP BY) cho bao cao. Database tinh, khong keo het du lieu ve Java. */
public interface ReportRepository extends Repository<Transaction, Long> {

    @Query("""
            select new com.kei.backend.report.CategoryTotal(c.id, c.name, c.icon, c.type, sum(t.amount))
            from Transaction t join t.category c
            where t.userId = :userId and t.occurredOn between :from and :to
            group by c.id, c.name, c.icon, c.type
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(Long userId, LocalDate from, LocalDate to);

    @Query("""
            select new com.kei.backend.report.DailyTypeTotal(t.occurredOn, c.type, sum(t.amount))
            from Transaction t join t.category c
            where t.userId = :userId and t.occurredOn between :from and :to
            group by t.occurredOn, c.type
            """)
    List<DailyTypeTotal> totalsByDay(Long userId, LocalDate from, LocalDate to);
}
