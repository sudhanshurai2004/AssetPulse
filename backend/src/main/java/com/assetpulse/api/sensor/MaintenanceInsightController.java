package com.assetpulse.api.sensor;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/assets")
public class MaintenanceInsightController {
    private final MaintenanceInsightService insights;

    public MaintenanceInsightController(MaintenanceInsightService insights) {
        this.insights = insights;
    }

    @GetMapping("/{assetId}/maintenance-insight")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public MaintenanceInsight suggest(
            @PathVariable Long assetId,
            @RequestParam @NotBlank String metric) {
        return insights.suggestThreshold(assetId, metric.trim());
    }
}
