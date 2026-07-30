package pl.klejczyk.tpm.workorder.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.klejczyk.tpm.workorder.domain.Actor;
import pl.klejczyk.tpm.workorder.domain.WorkOrder;
import pl.klejczyk.tpm.workorder.domain.exceptions.WorkOrderNotFound;
import pl.klejczyk.tpm.workorder.domain.WorkOrderReason;
import pl.klejczyk.tpm.workorder.domain.WorkOrderRepository;

import java.time.Clock;
import java.util.UUID;

@Service
public class WorkOrderService {

    private final WorkOrderRepository repository;
    private final Clock clock;

    public WorkOrderService(WorkOrderRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public WorkOrder report(String machineId, WorkOrderReason reason, String reportedBy) {
        WorkOrder workOrder = WorkOrder.report(
                UUID.randomUUID().toString(), machineId, reason, reportedBy, clock.instant());
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder assign(String id, Actor actor, String technicianId) {
        WorkOrder workOrder = load(id);
        workOrder.assign(actor, technicianId);
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder start(String id, Actor actor) {
        WorkOrder workOrder = load(id);
        workOrder.start(actor, clock.instant());
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder hold(String id, Actor actor, String reason) {
        WorkOrder workOrder = load(id);
        workOrder.hold(actor, reason);
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder resume(String id, Actor actor) {
        WorkOrder workOrder = load(id);
        workOrder.resume(actor);
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder resolve(String id, Actor actor, String resolution) {
        WorkOrder workOrder = load(id);
        workOrder.resolve(actor, resolution, clock.instant());
        return repository.save(workOrder);
    }

    @Transactional
    public WorkOrder close(String id, Actor actor) {
        WorkOrder workOrder = load(id);
        workOrder.close(actor);
        return repository.save(workOrder);
    }

    @Transactional(readOnly = true)
    public WorkOrder byId(String id) {
        return load(id);
    }

    private WorkOrder load(String id) {
        return repository.findById(id).orElseThrow(() -> new WorkOrderNotFound(id));
    }
}
