package com.carbontrack.emission;

import com.carbontrack.activity.ActivityCategory;
import com.carbontrack.common.DuplicateResourceException;
import com.carbontrack.common.ResourceNotFoundException;
import com.carbontrack.emission.dto.CreateEmissionFactorRequest;
import com.carbontrack.emission.dto.EmissionFactorResponse;
import com.carbontrack.emission.dto.UpdateEmissionFactorRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmissionFactorService {

    private final EmissionFactorRepository repository;
    private final Clock clock;

    /** Factors currently in force (one per activity type + unit), optionally filtered by category. */
    @Transactional(readOnly = true)
    public List<EmissionFactorResponse> currentFactors(ActivityCategory category) {
        Map<String, EmissionFactor> latest = new LinkedHashMap<>();
        repository.findByEffectiveDateLessThanEqualOrderByActivityTypeAscUnitAscEffectiveDateDesc(LocalDate.now(clock))
                .stream()
                .filter(f -> category == null || f.getCategory() == category)
                .forEach(f -> latest.putIfAbsent(f.getActivityType() + "|" + f.getUnit(), f));
        return latest.values().stream().map(EmissionFactorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmissionFactorResponse> allVersions() {
        return repository.findAllByOrderByCategoryAscActivityTypeAscEffectiveDateDesc().stream()
                .map(EmissionFactorResponse::from)
                .toList();
    }

    @Transactional
    public EmissionFactorResponse create(CreateEmissionFactorRequest request) {
        if (repository.existsByActivityTypeAndUnitAndEffectiveDate(
                request.activityType(), request.unit(), request.effectiveDate())) {
            throw new DuplicateResourceException("An emission factor for %s/%s effective %s already exists"
                    .formatted(request.activityType(), request.unit(), request.effectiveDate()));
        }
        EmissionFactor factor = new EmissionFactor();
        factor.setCategory(request.category());
        factor.setActivityType(request.activityType());
        factor.setUnit(request.unit());
        factor.setKgCo2ePerUnit(request.kgCo2ePerUnit());
        factor.setSource(request.source());
        factor.setEffectiveDate(request.effectiveDate());
        return EmissionFactorResponse.from(repository.save(factor));
    }

    @Transactional
    public EmissionFactorResponse update(Long id, UpdateEmissionFactorRequest request) {
        EmissionFactor factor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emission factor " + id + " not found"));
        factor.setKgCo2ePerUnit(request.kgCo2ePerUnit());
        factor.setSource(request.source());
        return EmissionFactorResponse.from(factor);
    }
}
