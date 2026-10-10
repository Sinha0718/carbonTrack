package com.carbontrack.activity;

import com.carbontrack.activity.dto.ActivityLogResponse;
import com.carbontrack.activity.dto.ElectricityActivityRequest;
import com.carbontrack.activity.dto.FoodActivityRequest;
import com.carbontrack.activity.dto.ShoppingActivityRequest;
import com.carbontrack.activity.dto.TransportActivityRequest;
import com.carbontrack.common.PageResponse;
import com.carbontrack.security.UserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogService activityLogService;

    @PostMapping("/transport")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLogResponse logTransport(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody TransportActivityRequest request) {
        return activityLogService.log(principal.getId(), request.mode(), request.distanceKm(),
                request.logDate(), request.notes());
    }

    @PostMapping("/electricity")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLogResponse logElectricity(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody ElectricityActivityRequest request) {
        return activityLogService.log(principal.getId(), request.energySource(), request.kwh(),
                request.logDate(), request.notes());
    }

    @PostMapping("/food")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLogResponse logFood(@AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody FoodActivityRequest request) {
        return activityLogService.log(principal.getId(), request.mealType(), request.servings(),
                request.logDate(), request.notes());
    }

    @PostMapping("/shopping")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLogResponse logShopping(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody ShoppingActivityRequest request) {
        return activityLogService.log(principal.getId(), request.productCategory(), request.spendAmount(),
                request.logDate(), request.notes());
    }

    @GetMapping
    public PageResponse<ActivityLogResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ActivityCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(size = 20, sort = {"logDate", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(activityLogService.list(principal.getId(), category, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ActivityLogResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return activityLogService.get(principal.getId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        activityLogService.delete(principal.getId(), id);
    }
}
