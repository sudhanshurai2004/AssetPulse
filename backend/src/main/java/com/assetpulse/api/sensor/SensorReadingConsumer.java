package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetRepository;
import com.assetpulse.api.common.ResourceNotFoundException;
import com.assetpulse.api.workorder.WorkOrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SensorReadingConsumer {
    private final SensorReadingRepository readings;
    private final AssetRepository assets;
    private final WorkOrderService workOrders;

    public SensorReadingConsumer(
            SensorReadingRepository readings, AssetRepository assets, WorkOrderService workOrders) {
        this.readings = readings;
        this.assets = assets;
        this.workOrders = workOrders;
    }

    @KafkaListener(topics = "${app.kafka.sensor-topic}")
    @Transactional
    public void consume(SensorReadingEvent event) {
        if (readings.existsByEventId(event.eventId())) {
            return;
        }
        Asset asset = assets.findById(event.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", event.assetId()));
        SensorReading reading = readings.saveAndFlush(new SensorReading(
                event.eventId(), asset, event.metric(), event.value(), event.recordedAt()));
        if (asset.getStatus() == com.assetpulse.api.asset.AssetStatus.ACTIVE
                && event.value() > asset.getThreshold()) {
            workOrders.createFromReading(asset, reading);
        }
    }
}
