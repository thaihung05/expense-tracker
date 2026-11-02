package com.kei.backend.budget;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @EntityGraph(attributePaths = "category")
    List<Budget> findByUserIdOrderById(Long userId);

    Optional<Budget> findByUserIdAndCategory_Id(Long userId, Long categoryId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}
