package com.assetpulse.api.workorder;

import java.time.Instant;

public record WorkOrderResponse(
        Long id,
        String title,
        String description,
        WorkOrderStatus status,
        WorkOrderPriority priority,
        Long assetId,
        String assetName,
        Long technicianId,
        String technicianName,
        Instant createdAt,
        Instant updatedAt) {
    public static WorkOrderResponse from(WorkOrder order) {
        return new WorkOrderResponse(
                order.getId(), order.getTitle(), order.getDescription(), order.getStatus(), order.getPriority(),
                order.getAsset().getId(), order.getAsset().getName(),
                order.getTechnician() == null ? null : order.getTechnician().getId(),
                order.getTechnician() == null ? null : order.getTechnician().getName(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
