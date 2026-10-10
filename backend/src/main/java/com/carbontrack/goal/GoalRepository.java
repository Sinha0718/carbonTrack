package com.carbontrack.goal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdAndStatus(Long userId, GoalStatus status);
}
