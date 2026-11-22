package com.assetpulse.api.sensor;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    boolean existsByEventId(UUID eventId);
    List<SensorReading> findTop50ByAsset_IdAndMetricOrderByRecordedAtDesc(Long assetId, String metric);
}
