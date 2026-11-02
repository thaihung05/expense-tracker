package com.kei.backend.quickentry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRuleRepository extends JpaRepository<KeywordRule, Long> {

    @EntityGraph(attributePaths = "category")
    List<KeywordRule> findByUserId(Long userId);

    Optional<KeywordRule> findByUserIdAndKeyword(Long userId, String keyword);
}
