package com.assetpulse.api.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetService;
import com.assetpulse.api.asset.AssetStatus;
import com.assetpulse.api.location.Location;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MaintenanceInsightServiceTest {
    private AssetService assets;
    private SensorReadingRepository readings;
    private MaintenanceInsightService service;

    @BeforeEach
    void setUp() {
        assets = mock(AssetService.class);
        readings = mock(SensorReadingRepository.class);
        service = new MaintenanceInsightService(assets, readings);
        when(assets.getEntity(1L)).thenReturn(
                new Asset("Pump", "P-1", "Pump", new Location("Plant"), AssetStatus.ACTIVE, 25));
    }

    @Test
    void suggestsTwoStandardDeviationsAboveMean() {
        when(readings.findTop50ByAsset_IdAndMetricOrderByRecordedAtDesc(1L, "temperature"))
                .thenReturn(List.of(10.0, 12.0, 14.0, 16.0, 18.0).stream()
                        .map(value -> new SensorReading(UUID.randomUUID(), null, "temperature", value, Instant.now()))
                        .toList());

        MaintenanceInsight insight = service.suggestThreshold(1L, "temperature");

        assertThat(insight.sampleCount()).isEqualTo(5);
        assertThat(insight.suggestedThreshold()).isCloseTo(14 + 2 * Math.sqrt(8), offset(0.0001));
        assertThat(insight.explanation()).contains("Review before changing");
    }

    @Test
    void withholdsSuggestionUntilMinimumSamplesExist() {
        when(readings.findTop50ByAsset_IdAndMetricOrderByRecordedAtDesc(1L, "temperature"))
                .thenReturn(List.of());

        MaintenanceInsight insight = service.suggestThreshold(1L, "temperature");

        assertThat(insight.suggestedThreshold()).isNull();
        assertThat(insight.currentThreshold()).isEqualTo(25);
        assertThat(insight.explanation()).contains("at least 5 readings");
    }
}
