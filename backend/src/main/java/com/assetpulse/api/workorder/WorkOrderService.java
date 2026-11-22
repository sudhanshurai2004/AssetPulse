package com.assetpulse.api.workorder;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetService;
import com.assetpulse.api.common.ResourceNotFoundException;
import com.assetpulse.api.sensor.SensorReading;
import com.assetpulse.api.technician.Technician;
import com.assetpulse.api.technician.TechnicianRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkOrderService {
    private final WorkOrderRepository workOrders;
    private final AssetService assets;
    private final TechnicianRepository technicians;

    public WorkOrderService(WorkOrderRepository workOrders, AssetService assets, TechnicianRepository technicians) {
        this.workOrders = workOrders;
        this.assets = assets;
        this.technicians = technicians;
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> list(WorkOrderStatus status, String query, Pageable pageable) {
        Specification<WorkOrder> specification = Specification.where(null);
        if (status != null) {
            specification = specification.and((root, criteria, builder) -> builder.equal(root.get("status"), status));
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteria, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), pattern),
                    builder.like(builder.lower(root.join("asset").get("name")), pattern)));
        }
        return workOrders.findAll(specification, pageable).map(WorkOrderResponse::from);
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest request) {
        Asset asset = assets.getEntity(request.assetId());
        Technician technician = request.technicianId() == null ? null : technicians.findById(request.technicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician", request.technicianId()));
        WorkOrderStatus status = technician == null ? WorkOrderStatus.OPEN : WorkOrderStatus.ASSIGNED;
        return WorkOrderResponse.from(workOrders.save(new WorkOrder(
                request.title().trim(), request.description().trim(), status, request.priority(),
                asset, technician, null)));
    }

    @Transactional
    public WorkOrderResponse transition(Long id, WorkOrderStatus newStatus) {
        WorkOrder order = workOrders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order", id));
        boolean valid = switch (order.getStatus()) {
            case OPEN -> newStatus == WorkOrderStatus.ASSIGNED;
            case ASSIGNED -> newStatus == WorkOrderStatus.IN_PROGRESS || newStatus == WorkOrderStatus.OPEN;
            case IN_PROGRESS -> newStatus == WorkOrderStatus.DONE;
            case DONE -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Cannot move work order from " + order.getStatus() + " to " + newStatus);
        }
        if (order.getStatus() == WorkOrderStatus.OPEN && newStatus == WorkOrderStatus.ASSIGNED) {
            Technician technician = technicians.findFirstByActiveTrueOrderByIdAsc()
                    .orElseThrow(() -> new IllegalStateException("No active technician is available"));
            order.assignTo(technician);
        }
        order.transitionTo(newStatus);
        return WorkOrderResponse.from(order);
    }

    @Transactional
    public void createFromReading(Asset asset, SensorReading reading) {
        if (workOrders.existsBySourceReadingId(reading.getId())) {
            return;
        }
        Technician technician = technicians.findFirstByActiveTrueOrderByIdAsc().orElse(null);
        WorkOrderStatus status = technician == null ? WorkOrderStatus.OPEN : WorkOrderStatus.ASSIGNED;
        WorkOrder order = new WorkOrder(
                "Sensor threshold exceeded: " + asset.getName(),
                reading.getMetric() + " reading " + reading.getValue()
                        + " exceeded configured threshold " + asset.getThreshold() + ".",
                status,
                reading.getValue() >= asset.getThreshold() * 1.5
                        ? WorkOrderPriority.CRITICAL : WorkOrderPriority.HIGH,
                asset, technician, reading);
        workOrders.save(order);
    }
}
