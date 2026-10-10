package com.carbontrack.emission;

import com.carbontrack.activity.ActivityCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A configurable kg CO2e-per-unit factor for one activity type. Factors live in the database
 * (not in code) so they can be revised as IPCC/EPA tables change without a redeploy.
 */
@Entity
@Table(name = "emission_factors")
@Getter
@Setter
@NoArgsConstructor
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityCategory category;

    @Column(name = "activity_type", nullable = false, length = 64)
    private String activityType;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(name = "kg_co2e_per_unit", nullable = false, precision = 12, scale = 6)
    private BigDecimal kgCo2ePerUnit;

    @Column(nullable = false)
    private String source;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
