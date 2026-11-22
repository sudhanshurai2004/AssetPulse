package com.assetpulse.api.sensor;

import java.time.Instant;
import java.util.UUID;

public record SensorReadingEvent(UUID eventId, Long assetId, String metric, double value, Instant recordedAt) {
}
