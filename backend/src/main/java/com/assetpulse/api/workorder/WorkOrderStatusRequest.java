package com.assetpulse.api.workorder;

import jakarta.validation.constraints.NotNull;

public record WorkOrderStatusRequest(@NotNull WorkOrderStatus status) {
}
