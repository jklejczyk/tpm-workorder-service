package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import java.time.Instant;

public record WorkOrderResolved(
        String workOrderId,
        String machineId,
        Instant startedAt,
        Instant resolvedAt) {
}
