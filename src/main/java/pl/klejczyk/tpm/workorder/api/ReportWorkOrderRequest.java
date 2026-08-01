package pl.klejczyk.tpm.workorder.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.klejczyk.tpm.workorder.domain.WorkOrderReason;

public record ReportWorkOrderRequest(
        @NotBlank String machineId,
        @NotNull WorkOrderReason reason) {
}
