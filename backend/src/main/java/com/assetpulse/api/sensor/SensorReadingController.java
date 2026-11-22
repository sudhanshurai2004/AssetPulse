package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensor-readings")
public class SensorReadingController {
    private final AssetService assets;
    private final SensorReadingIngestionService ingestion;

    public SensorReadingController(AssetService assets, SensorReadingIngestionService ingestion) {
        this.assets = assets;
        this.ingestion = ingestion;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> publish(@Valid @RequestBody SensorReadingRequest request) {
        assets.getEntity(request.assetId());
        SensorReadingEvent event = new SensorReadingEvent(
                request.eventId(), request.assetId(), request.metric().trim(),
                request.value(), request.recordedAt());
        ingestion.ingest(event);
        return ResponseEntity.status(ingestion.asynchronous() ? HttpStatus.ACCEPTED : HttpStatus.CREATED).build();
    }
}
