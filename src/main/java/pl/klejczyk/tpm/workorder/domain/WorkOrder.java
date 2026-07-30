package pl.klejczyk.tpm.workorder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pl.klejczyk.tpm.workorder.domain.exception.IllegalStateTransition;
import pl.klejczyk.tpm.workorder.domain.exception.MissingHoldReason;
import pl.klejczyk.tpm.workorder.domain.exception.MissingResolution;
import pl.klejczyk.tpm.workorder.domain.exception.UnauthorizedTransition;

import java.time.Instant;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    private String id;

    private String machineId;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status;

    @Enumerated(EnumType.STRING)
    private WorkOrderReason reason;

    private String reportedBy;
    private String assignedTo;
    private String resolution;
    private String holdReason;
    private Instant reportedAt;
    private Instant startedAt;
    private Instant resolvedAt;

    protected WorkOrder() {
    }

    private WorkOrder(String id, String machineId, WorkOrderReason reason, String reportedBy, Instant reportedAt) {
        this.id = id;
        this.machineId = machineId;
        this.reason = reason;
        this.reportedBy = reportedBy;
        this.reportedAt = reportedAt;
        this.status = WorkOrderStatus.REPORTED;
    }

    public static WorkOrder report(String id, String machineId, WorkOrderReason reason, String reportedBy, Instant reportedAt) {
        return new WorkOrder(id, machineId, reason, reportedBy, reportedAt);
    }

    public void assign(Actor actor, String technicianId) {
        if (status != WorkOrderStatus.REPORTED) {
            throw IllegalStateTransition.from(status, "assign");
        }
        if (!actor.isAnyOf(Role.MANAGER, Role.TECHNICIAN)) {
            throw UnauthorizedTransition.forActor(actor, "assign");
        }
        this.assignedTo = technicianId;
        this.status = WorkOrderStatus.ASSIGNED;
    }

    public void start(Actor actor, Instant now) {
        if (status != WorkOrderStatus.ASSIGNED) {
            throw IllegalStateTransition.from(status, "start");
        }
        if (!actor.hasRole(Role.TECHNICIAN) || !actor.id().equals(assignedTo)) {
            throw UnauthorizedTransition.forActor(actor, "start");
        }
        this.startedAt = now;
        this.status = WorkOrderStatus.IN_PROGRESS;
    }

    public void hold(Actor actor, String reason) {
        if (status != WorkOrderStatus.IN_PROGRESS) {
            throw IllegalStateTransition.from(status, "hold");
        }
        if (!actor.hasRole(Role.TECHNICIAN)) {
            throw UnauthorizedTransition.forActor(actor, "hold");
        }
        if (reason == null || reason.isBlank()) {
            throw new MissingHoldReason();
        }
        this.holdReason = reason;
        this.status = WorkOrderStatus.ON_HOLD;
    }

    public void resume(Actor actor) {
        if (status != WorkOrderStatus.ON_HOLD) {
            throw IllegalStateTransition.from(status, "resume");
        }
        if (!actor.hasRole(Role.TECHNICIAN)) {
            throw UnauthorizedTransition.forActor(actor, "resume");
        }
        this.holdReason = null;
        this.status = WorkOrderStatus.IN_PROGRESS;
    }

    public void resolve(Actor actor, String resolution, Instant now) {
        if (status != WorkOrderStatus.IN_PROGRESS) {
            throw IllegalStateTransition.from(status, "resolve");
        }
        if (!actor.hasRole(Role.TECHNICIAN)) {
            throw UnauthorizedTransition.forActor(actor, "resolve");
        }
        if (resolution == null || resolution.isBlank()) {
            throw new MissingResolution();
        }
        this.resolution = resolution;
        this.resolvedAt = now;
        this.status = WorkOrderStatus.RESOLVED;
    }

    public void close(Actor actor) {
        if (status != WorkOrderStatus.RESOLVED) {
            throw IllegalStateTransition.from(status, "close");
        }
        if (!actor.hasRole(Role.MANAGER)) {
            throw UnauthorizedTransition.forActor(actor, "close");
        }
        this.status = WorkOrderStatus.CLOSED;
    }

    public String id() {
        return id;
    }

    public String machineId() {
        return machineId;
    }

    public WorkOrderStatus status() {
        return status;
    }

    public String assignedTo() {
        return assignedTo;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant resolvedAt() {
        return resolvedAt;
    }
}
