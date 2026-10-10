package com.carbontrack.activity;

import com.carbontrack.activity.dto.ActivityLogResponse;
import com.carbontrack.common.ResourceNotFoundException;
import com.carbontrack.emission.EmissionCalculationService;
import com.carbontrack.emission.EmissionResult;
import com.carbontrack.user.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final LocalDate EARLIEST_DATE = LocalDate.of(1970, 1, 1);

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final EmissionCalculationService emissionCalculationService;
    private final Clock clock;

    @Transactional
    public ActivityLogResponse log(Long userId, ActivityType type, BigDecimal quantity, LocalDate logDate, String notes) {
        LocalDate date = logDate != null ? logDate : LocalDate.now(clock);
        EmissionResult emission = emissionCalculationService.calculate(type.name(), type.unit(), quantity, date);

        ActivityLog log = new ActivityLog();
        log.setUser(userRepository.getReferenceById(userId));
        log.setCategory(type.category());
        log.setActivityType(type.name());
        log.setQuantity(quantity);
        log.setUnit(type.unit());
        log.setCo2eKg(emission.co2eKg());
        log.setEmissionFactor(emission.factor());
        log.setLogDate(date);
        log.setNotes(notes);
        return ActivityLogResponse.from(activityLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> list(
            Long userId, ActivityCategory category, LocalDate from, LocalDate to, Pageable pageable) {
        LocalDate start = from != null ? from : EARLIEST_DATE;
        LocalDate end = to != null ? to : LocalDate.now(clock);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        Page<ActivityLog> page = category == null
                ? activityLogRepository.findByUserIdAndLogDateBetween(userId, start, end, pageable)
                : activityLogRepository.findByUserIdAndCategoryAndLogDateBetween(userId, category, start, end, pageable);
        return page.map(ActivityLogResponse::from);
    }

    @Transactional(readOnly = true)
    public ActivityLogResponse get(Long userId, Long id) {
        return ActivityLogResponse.from(findOwned(userId, id));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        activityLogRepository.delete(findOwned(userId, id));
    }

    private ActivityLog findOwned(Long userId, Long id) {
        return activityLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity log " + id + " not found"));
    }
}
