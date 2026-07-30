package pl.klejczyk.tpm.workorder.domain;

import org.junit.jupiter.api.Test;
import pl.klejczyk.tpm.workorder.domain.exception.IllegalStateTransition;
import pl.klejczyk.tpm.workorder.domain.exception.MissingHoldReason;
import pl.klejczyk.tpm.workorder.domain.exception.MissingResolution;
import pl.klejczyk.tpm.workorder.domain.exception.UnauthorizedTransition;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkOrderTest {

    private static final Instant NOW = Instant.parse("2026-08-01T10:00:00Z");

    private static final Actor MANAGER = new Actor("mgr-1", Role.MANAGER);
    private static final Actor TECHNICIAN = new Actor("tech-1", Role.TECHNICIAN);
    private static final Actor OTHER_TECHNICIAN = new Actor("tech-2", Role.TECHNICIAN);
    private static final Actor OPERATOR = new Actor("op-1", Role.OPERATOR);

    private WorkOrder reported() {
        return WorkOrder.report("wo-1", "m-1", WorkOrderReason.BREAKDOWN, "op-1", NOW);
    }

    private WorkOrder inProgress() {
        WorkOrder workOrder = reported();
        workOrder.assign(MANAGER, "tech-1");
        workOrder.start(TECHNICIAN, NOW.plusSeconds(60));
        return workOrder;
    }

    @Test
    void isReportedWhenCreated() {
        assertThat(reported().status()).isEqualTo(WorkOrderStatus.REPORTED);
    }

    @Test
    void managerAssignsTechnician() {
        WorkOrder workOrder = reported();

        workOrder.assign(MANAGER, "tech-1");

        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.ASSIGNED);
        assertThat(workOrder.assignedTo()).isEqualTo("tech-1");
    }

    @Test
    void operatorCannotAssign() {
        assertThatThrownBy(() -> reported().assign(OPERATOR, "tech-1")).isInstanceOf(UnauthorizedTransition.class);
    }

    @Test
    void cannotStartWhileStillReported() {
        assertThatThrownBy(() -> reported().start(TECHNICIAN, NOW)).isInstanceOf(IllegalStateTransition.class);
    }

    @Test
    void onlyAssignedTechnicianCanStart() {
        WorkOrder workOrder = reported();
        workOrder.assign(MANAGER, "tech-1");

        assertThatThrownBy(() -> workOrder.start(OTHER_TECHNICIAN, NOW)).isInstanceOf(UnauthorizedTransition.class);
    }

    @Test
    void startRecordsStartTime() {
        WorkOrder workOrder = inProgress();

        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(workOrder.startedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void holdRequiresReason() {
        assertThatThrownBy(() -> inProgress().hold(TECHNICIAN, "   ")).isInstanceOf(MissingHoldReason.class);
    }

    @Test
    void holdAndResumeReturnsToInProgress() {
        WorkOrder workOrder = inProgress();

        workOrder.hold(TECHNICIAN, "Spare part unavailable");
        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.ON_HOLD);

        workOrder.resume(TECHNICIAN);
        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
    }

    @Test
    void resolveRequiresDescription() {
        assertThatThrownBy(() -> inProgress().resolve(TECHNICIAN, "", NOW.plusSeconds(600))).isInstanceOf(MissingResolution.class);
    }

    @Test
    void resolveRecordsResolutionTime() {
        WorkOrder workOrder = inProgress();

        workOrder.resolve(TECHNICIAN, "Bearing replaced", NOW.plusSeconds(600));

        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.RESOLVED);
        assertThat(workOrder.resolvedAt()).isEqualTo(NOW.plusSeconds(600));
    }

    @Test
    void technicianCannotClose() {
        WorkOrder workOrder = inProgress();
        workOrder.resolve(TECHNICIAN, "Bearing replaced", NOW.plusSeconds(600));

        assertThatThrownBy(() -> workOrder.close(TECHNICIAN)).isInstanceOf(UnauthorizedTransition.class);
    }

    @Test
    void closedIsTerminal() {
        WorkOrder workOrder = inProgress();
        workOrder.resolve(TECHNICIAN, "Bearing replaced", NOW.plusSeconds(600));

        workOrder.close(MANAGER);
        assertThat(workOrder.status()).isEqualTo(WorkOrderStatus.CLOSED);

        assertThatThrownBy(() -> workOrder.assign(MANAGER, "tech-2")).isInstanceOf(IllegalStateTransition.class);
    }
}
