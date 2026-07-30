package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import java.time.Instant;

public record WorkOrderStarted(String workOrderId, String machineId, Instant startedAt) {
}
