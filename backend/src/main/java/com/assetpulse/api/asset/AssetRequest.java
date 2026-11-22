package com.assetpulse.api.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AssetRequest(
        @NotBlank String name,
        @NotBlank String assetTag,
        @NotBlank String type,
        @NotBlank String location,
        @NotNull AssetStatus status,
        @PositiveOrZero double threshold) {
}
