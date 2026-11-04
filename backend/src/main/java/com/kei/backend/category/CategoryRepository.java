package com.kei.backend.category;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Danh muc mac dinh + danh muc rieng cua nguoi dung. */
    @Query("select c from Category c where c.userId is null or c.userId = :userId order by c.id")
    List<Category> findVisible(Long userId);

    @Query("select c from Category c where (c.userId is null or c.userId = :userId) and c.type = :type order by c.id")
    List<Category> findVisibleByType(Long userId, CategoryType type);

    List<Category> findByUserIdIsNull();

    List<Category> findByUserIdOrderById(Long userId);

    @Query("select count(t) > 0 from Transaction t where t.category.id = :categoryId")
    boolean isUsedByTransactions(Long categoryId);
}
