package com.carbontrack.emission;

import com.carbontrack.activity.ActivityCategory;
import com.carbontrack.emission.dto.EmissionFactorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emission-factors")
@RequiredArgsConstructor
public class EmissionFactorController {

    private final EmissionFactorService emissionFactorService;

    @GetMapping
    public List<EmissionFactorResponse> currentFactors(@RequestParam(required = false) ActivityCategory category) {
        return emissionFactorService.currentFactors(category);
    }
}
