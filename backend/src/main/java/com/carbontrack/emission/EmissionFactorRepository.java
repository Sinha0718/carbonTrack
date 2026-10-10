package com.carbontrack.emission;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {

    /** The factor version in force on the given date: latest effective_date not after it. */
    Optional<EmissionFactor> findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String activityType, String unit, LocalDate date);

    List<EmissionFactor> findByEffectiveDateLessThanEqualOrderByActivityTypeAscUnitAscEffectiveDateDesc(LocalDate date);

    List<EmissionFactor> findAllByOrderByCategoryAscActivityTypeAscEffectiveDateDesc();

    boolean existsByActivityTypeAndUnitAndEffectiveDate(String activityType, String unit, LocalDate effectiveDate);
}
