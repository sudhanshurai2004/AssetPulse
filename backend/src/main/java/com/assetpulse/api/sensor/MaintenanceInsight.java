package com.assetpulse.api.sensor;

public record MaintenanceInsight(
        Long assetId,
        String assetName,
        String metric,
        int sampleCount,
        double currentThreshold,
        Double suggestedThreshold,
        String explanation) {
}
