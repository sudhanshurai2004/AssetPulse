package com.assetpulse.api.asset;

import com.assetpulse.api.location.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "asset_tag", nullable = false, unique = true, length = 80)
    private String assetTag;

    @Column(nullable = false, length = 80)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AssetStatus status;

    @Column(nullable = false)
    private double threshold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Asset() {
    }

    public Asset(String name, String assetTag, String type, Location location, AssetStatus status, double threshold) {
        this.name = name;
        this.assetTag = assetTag;
        this.type = type;
        this.location = location;
        this.status = status;
        this.threshold = threshold;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void update(String name, String assetTag, String type, Location location, AssetStatus status, double threshold) {
        this.name = name;
        this.assetTag = assetTag;
        this.type = type;
        this.location = location;
        this.status = status;
        this.threshold = threshold;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAssetTag() { return assetTag; }
    public String getType() { return type; }
    public Location getLocation() { return location; }
    public AssetStatus getStatus() { return status; }
    public double getThreshold() { return threshold; }
    public Instant getCreatedAt() { return createdAt; }
}
