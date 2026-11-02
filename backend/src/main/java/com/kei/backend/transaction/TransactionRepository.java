package com.kei.backend.transaction;

import com.kei.backend.category.CategoryType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Tim giao dich cua 1 nguoi trong khoang ngay, loc them theo danh muc / loai neu co.
     * "join fetch" lay luon danh muc trong cung 1 cau SQL (tranh loi N+1 query).
     */
    @Query(value = """
            select t from Transaction t join fetch t.category c
            where t.userId = :userId
              and t.occurredOn between :from and :to
              and (:categoryId is null or c.id = :categoryId)
              and (:type is null or c.type = :type)
            order by t.occurredOn desc, t.id desc
            """,
            countQuery = """
            select count(t) from Transaction t join t.category c
            where t.userId = :userId
              and t.occurredOn between :from and :to
              and (:categoryId is null or c.id = :categoryId)
              and (:type is null or c.type = :type)
            """)
    Page<Transaction> search(Long userId, LocalDate from, LocalDate to,
            Long categoryId, CategoryType type, Pageable pageable);

    @Query("""
            select t from Transaction t join fetch t.category
            where t.userId = :userId and t.occurredOn between :from and :to
            order by t.occurredOn, t.id
            """)
    List<Transaction> findAllInRange(Long userId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
