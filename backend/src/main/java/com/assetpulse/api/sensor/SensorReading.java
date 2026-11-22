package com.assetpulse.api.sensor;

import com.assetpulse.api.asset.Asset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false, length = 80)
    private String metric;

    @Column(nullable = false)
    private double value;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected SensorReading() {
    }

    public SensorReading(UUID eventId, Asset asset, String metric, double value, Instant recordedAt) {
        this.eventId = eventId;
        this.asset = asset;
        this.metric = metric;
        this.value = value;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public UUID getEventId() { return eventId; }
    public Asset getAsset() { return asset; }
    public String getMetric() { return metric; }
    public double getValue() { return value; }
    public Instant getRecordedAt() { return recordedAt; }
}
