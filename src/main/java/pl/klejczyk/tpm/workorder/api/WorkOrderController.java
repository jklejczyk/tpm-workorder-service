package pl.klejczyk.tpm.workorder.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.klejczyk.tpm.workorder.application.WorkOrderService;

@RestController
@RequestMapping("/work-orders")
class WorkOrderController {

    private final WorkOrderService service;

    WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<WorkOrderResponse> report(@Valid @RequestBody ReportWorkOrderRequest request) {
        WorkOrderResponse response = WorkOrderResponse.from(service.report(request.machineId(), request.reason(), request.reportedBy()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/assign")
    WorkOrderResponse assign(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole, @Valid @RequestBody AssignRequest request) {
        return WorkOrderResponse.from(service.assign(id, ActorFactory.from(actorId, actorRole), request.technicianId()));
    }

    @PostMapping("/{id}/start")
    WorkOrderResponse start(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole) {
        return WorkOrderResponse.from(service.start(id, ActorFactory.from(actorId, actorRole)));
    }

    @PostMapping("/{id}/hold")
    WorkOrderResponse hold(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole, @Valid @RequestBody HoldRequest request) {
        return WorkOrderResponse.from(service.hold(id, ActorFactory.from(actorId, actorRole), request.reason()));
    }

    @PostMapping("/{id}/resume")
    WorkOrderResponse resume(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole) {
        return WorkOrderResponse.from(service.resume(id, ActorFactory.from(actorId, actorRole)));
    }

    @PostMapping("/{id}/resolve")
    WorkOrderResponse resolve(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole, @Valid @RequestBody ResolveRequest request) {
        return WorkOrderResponse.from(service.resolve(id, ActorFactory.from(actorId, actorRole), request.resolution()));
    }

    @PostMapping("/{id}/close")
    WorkOrderResponse close(@PathVariable String id, @RequestHeader("X-Actor-Id") String actorId, @RequestHeader("X-Actor-Role") String actorRole) {
        return WorkOrderResponse.from(service.close(id, ActorFactory.from(actorId, actorRole)));
    }

    @GetMapping("/{id}")
    WorkOrderResponse byId(@PathVariable String id) {
        return WorkOrderResponse.from(service.byId(id));
    }
}
