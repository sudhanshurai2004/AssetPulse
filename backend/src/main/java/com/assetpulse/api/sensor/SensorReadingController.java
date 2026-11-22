package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.AssetService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensor-readings")
public class SensorReadingController {
    private final AssetService assets;
    private final KafkaTemplate<Object, Object> kafka;
    private final String topic;

    public SensorReadingController(
            AssetService assets,
            KafkaTemplate<Object, Object> kafka,
            @Value("${app.kafka.sensor-topic}") String topic) {
        this.assets = assets;
        this.kafka = kafka;
        this.topic = topic;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> publish(@Valid @RequestBody SensorReadingRequest request) {
        assets.getEntity(request.assetId());
        UUID eventId = request.eventId();
        SensorReadingEvent event = new SensorReadingEvent(
                eventId, request.assetId(), request.metric().trim(), request.value(), request.recordedAt());
        kafka.send(topic, eventId.toString(), event).join();
        return ResponseEntity.accepted().location(URI.create("/api/sensor-readings/" + eventId)).build();
    }
}
