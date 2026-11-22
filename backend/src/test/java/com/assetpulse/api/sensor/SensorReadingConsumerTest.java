package com.assetpulse.api.sensor;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetRepository;
import com.assetpulse.api.asset.AssetStatus;
import com.assetpulse.api.location.Location;
import com.assetpulse.api.workorder.WorkOrderService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensorReadingConsumerTest {
    private SensorReadingRepository readings;
    private AssetRepository assets;
    private WorkOrderService workOrders;
    private SensorReadingConsumer consumer;

    @BeforeEach
    void setUp() {
        readings = org.mockito.Mockito.mock(SensorReadingRepository.class);
        assets = org.mockito.Mockito.mock(AssetRepository.class);
        workOrders = org.mockito.Mockito.mock(WorkOrderService.class);
        consumer = new SensorReadingConsumer(readings, assets, workOrders);
    }

    @Test
    void skipsDuplicateEvents() {
        UUID eventId = UUID.randomUUID();
        when(readings.existsByEventId(eventId)).thenReturn(true);

        consumer.consume(new SensorReadingEvent(eventId, 10L, "temperature", 90, Instant.now()));

        verify(assets, never()).findById(10L);
        verify(workOrders, never()).createFromReading(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsWorkOrderForActiveAssetAboveThreshold() {
        UUID eventId = UUID.randomUUID();
        Asset asset = new Asset("Pump", "P-1", "Pump", new Location("Plant"), AssetStatus.ACTIVE, 80);
        SensorReading reading = new SensorReading(eventId, asset, "temperature", 90, Instant.now());
        when(readings.existsByEventId(eventId)).thenReturn(false);
        when(assets.findById(10L)).thenReturn(java.util.Optional.of(asset));
        when(readings.saveAndFlush(org.mockito.ArgumentMatchers.any(SensorReading.class))).thenReturn(reading);

        consumer.consume(new SensorReadingEvent(eventId, 10L, "temperature", 90, Instant.now()));

        verify(workOrders).createFromReading(asset, reading);
    }

    @Test
    void doesNotCreateWorkOrderForReadingAtThreshold() {
        UUID eventId = UUID.randomUUID();
        Asset asset = new Asset("Pump", "P-1", "Pump", new Location("Plant"), AssetStatus.ACTIVE, 80);
        when(readings.existsByEventId(eventId)).thenReturn(false);
        when(assets.findById(10L)).thenReturn(java.util.Optional.of(asset));
        when(readings.saveAndFlush(org.mockito.ArgumentMatchers.any(SensorReading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        consumer.consume(new SensorReadingEvent(eventId, 10L, "temperature", 80, Instant.now()));

        verify(workOrders, never()).createFromReading(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
