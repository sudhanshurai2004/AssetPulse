package com.assetpulse.api.workorder;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.sensor.SensorReading;
import com.assetpulse.api.technician.Technician;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "work_orders")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private WorkOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WorkOrderPriority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private Technician technician;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reading_id", unique = true)
    private SensorReading sourceReading;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkOrder() {
    }

    public WorkOrder(String title, String description, WorkOrderStatus status, WorkOrderPriority priority,
                     Asset asset, Technician technician, SensorReading sourceReading) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.asset = asset;
        this.technician = technician;
        this.sourceReading = sourceReading;
    }

    @PrePersist
    void setCreatedAt() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void setUpdatedAt() {
        updatedAt = Instant.now();
    }

    public void transitionTo(WorkOrderStatus newStatus) {
        this.status = newStatus;
    }

    public void assignTo(Technician technician) {
        this.technician = technician;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public WorkOrderStatus getStatus() { return status; }
    public WorkOrderPriority getPriority() { return priority; }
    public Asset getAsset() { return asset; }
    public Technician getTechnician() { return technician; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
