package com.carbontrack.emission;

import com.carbontrack.emission.dto.CreateEmissionFactorRequest;
import com.carbontrack.emission.dto.EmissionFactorResponse;
import com.carbontrack.emission.dto.UpdateEmissionFactorRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only management of emission factors (secured by /api/admin/** in SecurityConfig). */
@RestController
@RequestMapping("/api/admin/emission-factors")
@RequiredArgsConstructor
public class AdminEmissionFactorController {

    private final EmissionFactorService emissionFactorService;

    @GetMapping
    public List<EmissionFactorResponse> allVersions() {
        return emissionFactorService.allVersions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmissionFactorResponse create(@Valid @RequestBody CreateEmissionFactorRequest request) {
        return emissionFactorService.create(request);
    }

    @PutMapping("/{id}")
    public EmissionFactorResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEmissionFactorRequest request) {
        return emissionFactorService.update(id, request);
    }
}
