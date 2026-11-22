package com.assetpulse.api.workorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetService;
import com.assetpulse.api.asset.AssetStatus;
import com.assetpulse.api.common.ResourceNotFoundException;
import com.assetpulse.api.location.Location;
import com.assetpulse.api.technician.TechnicianRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkOrderServiceTest {
    private WorkOrderRepository workOrders;
    private WorkOrderService service;

    @BeforeEach
    void setUp() {
        workOrders = mock(WorkOrderRepository.class);
        service = new WorkOrderService(workOrders, mock(AssetService.class), mock(TechnicianRepository.class));
    }

    @Test
    void advancesAssignedOrderToInProgress() {
        Asset asset = new Asset("Pump", "P-1", "Pump", new Location("Plant"), AssetStatus.ACTIVE, 80);
        WorkOrder order = new WorkOrder(
                "Inspect pump", "Check vibration", WorkOrderStatus.ASSIGNED,
                WorkOrderPriority.HIGH, asset, null, null);
        when(workOrders.findById(1L)).thenReturn(Optional.of(order));

        WorkOrderResponse response = service.transition(1L, WorkOrderStatus.IN_PROGRESS);

        assertEquals(WorkOrderStatus.IN_PROGRESS, response.status());
    }

    @Test
    void rejectsSkippingWorkOrderSteps() {
        Asset asset = new Asset("Pump", "P-1", "Pump", new Location("Plant"), AssetStatus.ACTIVE, 80);
        WorkOrder order = new WorkOrder(
                "Inspect pump", "Check vibration", WorkOrderStatus.OPEN,
                WorkOrderPriority.HIGH, asset, null, null);
        when(workOrders.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> service.transition(1L, WorkOrderStatus.DONE));
    }

    @Test
    void reportsMissingWorkOrder() {
        when(workOrders.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.transition(404L, WorkOrderStatus.DONE));
    }
}
