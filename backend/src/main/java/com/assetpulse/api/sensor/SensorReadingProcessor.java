package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetRepository;
import com.assetpulse.api.asset.AssetStatus;
import com.assetpulse.api.common.ResourceNotFoundException;
import com.assetpulse.api.workorder.WorkOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorReadingProcessor {
    private final SensorReadingRepository readings;
    private final AssetRepository assets;
    private final WorkOrderService workOrders;

    public SensorReadingProcessor(
            SensorReadingRepository readings, AssetRepository assets, WorkOrderService workOrders) {
        this.readings = readings;
        this.assets = assets;
        this.workOrders = workOrders;
    }

    @Transactional
    public void process(SensorReadingEvent event) {
        if (readings.existsByEventId(event.eventId())) {
            return;
        }
        Asset asset = assets.findById(event.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", event.assetId()));
        SensorReading reading = readings.saveAndFlush(new SensorReading(
                event.eventId(), asset, event.metric(), event.value(), event.recordedAt()));
        if (asset.getStatus() == AssetStatus.ACTIVE && event.value() > asset.getThreshold()) {
            workOrders.createFromReading(asset, reading);
        }
    }
}
