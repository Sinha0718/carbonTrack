package com.carbontrack.activity;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @EntityGraph(attributePaths = "emissionFactor")
    Page<ActivityLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = "emissionFactor")
    Page<ActivityLog> findByUserIdAndCategoryAndLogDateBetween(
            Long userId, ActivityCategory category, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = "emissionFactor")
    Optional<ActivityLog> findByIdAndUserId(Long id, Long userId);
}
