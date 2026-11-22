package com.assetpulse.api.workorder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkOrderRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull WorkOrderPriority priority,
        @NotNull @Positive Long assetId,
        Long technicianId) {
}
