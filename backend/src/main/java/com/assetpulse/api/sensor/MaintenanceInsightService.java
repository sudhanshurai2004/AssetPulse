package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceInsightService {
    private static final int MINIMUM_SAMPLES = 5;

    private final AssetService assets;
    private final SensorReadingRepository readings;

    public MaintenanceInsightService(AssetService assets, SensorReadingRepository readings) {
        this.assets = assets;
        this.readings = readings;
    }

    @Transactional(readOnly = true)
    public MaintenanceInsight suggestThreshold(Long assetId, String metric) {
        Asset asset = assets.getEntity(assetId);
        List<SensorReading> samples =
                readings.findTop50ByAsset_IdAndMetricOrderByRecordedAtDesc(assetId, metric);
        if (samples.size() < MINIMUM_SAMPLES) {
            return new MaintenanceInsight(assetId, asset.getName(), metric, samples.size(),
                    asset.getThreshold(), null,
                    "Collect at least " + MINIMUM_SAMPLES + " readings for this metric before suggesting a threshold.");
        }

        double mean = samples.stream().mapToDouble(SensorReading::getValue).average().orElseThrow();
        double variance = samples.stream()
                .mapToDouble(reading -> Math.pow(reading.getValue() - mean, 2))
                .average()
                .orElseThrow();
        double suggestedThreshold = Math.max(0, mean + 2 * Math.sqrt(variance));
        return new MaintenanceInsight(assetId, asset.getName(), metric, samples.size(),
                asset.getThreshold(), suggestedThreshold,
                "Heuristic based on the mean plus two standard deviations from the latest "
                        + samples.size() + " readings. Review before changing the asset rule.");
    }
}
