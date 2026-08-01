package pl.klejczyk.tpm.workorder.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    ResponseEntity<WorkOrderResponse> report(@AuthenticationPrincipal Jwt token, @Valid @RequestBody ReportWorkOrderRequest request) {
        WorkOrderResponse response = WorkOrderResponse.from(service.report(request.machineId(), request.reason(), ActorFactory.from(token).id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/assign")
    WorkOrderResponse assign(@PathVariable String id,
                             @AuthenticationPrincipal Jwt token,
                             @Valid @RequestBody AssignRequest request) {
        return WorkOrderResponse.from(
                service.assign(id, ActorFactory.from(token), request.technicianId()));
    }

    @PostMapping("/{id}/start")
    WorkOrderResponse start(@PathVariable String id, @AuthenticationPrincipal Jwt token) {
        return WorkOrderResponse.from(service.start(id, ActorFactory.from(token)));
    }

    @PostMapping("/{id}/hold")
    WorkOrderResponse hold(@PathVariable String id,
                           @AuthenticationPrincipal Jwt token,
                           @Valid @RequestBody HoldRequest request) {
        return WorkOrderResponse.from(
                service.hold(id, ActorFactory.from(token), request.reason()));
    }

    @PostMapping("/{id}/resume")
    WorkOrderResponse resume(@PathVariable String id, @AuthenticationPrincipal Jwt token) {
        return WorkOrderResponse.from(service.resume(id, ActorFactory.from(token)));
    }

    @PostMapping("/{id}/resolve")
    WorkOrderResponse resolve(@PathVariable String id,
                              @AuthenticationPrincipal Jwt token,
                              @Valid @RequestBody ResolveRequest request) {
        return WorkOrderResponse.from(
                service.resolve(id, ActorFactory.from(token), request.resolution()));
    }

    @PostMapping("/{id}/close")
    WorkOrderResponse close(@PathVariable String id, @AuthenticationPrincipal Jwt token) {
        return WorkOrderResponse.from(service.close(id, ActorFactory.from(token)));
    }

    @GetMapping("/{id}")
    WorkOrderResponse byId(@PathVariable String id) {
        return WorkOrderResponse.from(service.byId(id));
    }
}
