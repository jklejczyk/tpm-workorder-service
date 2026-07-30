package pl.klejczyk.tpm.workorder.api;

import pl.klejczyk.tpm.workorder.domain.WorkOrder;

import java.time.Instant;

public record WorkOrderResponse(
        String id,
        String machineId,
        String status,
        String assignedTo,
        Instant startedAt,
        Instant resolvedAt) {

    public static WorkOrderResponse from(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.id(),
                workOrder.machineId(),
                workOrder.status().name(),
                workOrder.assignedTo(),
                workOrder.startedAt(),
                workOrder.resolvedAt());
    }
}