package pl.klejczyk.tpm.workorder.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pl.klejczyk.tpm.workorder.TestcontainersConfiguration;
import pl.klejczyk.tpm.workorder.domain.Actor;
import pl.klejczyk.tpm.workorder.domain.Role;
import pl.klejczyk.tpm.workorder.domain.WorkOrder;
import pl.klejczyk.tpm.workorder.domain.WorkOrderReason;
import pl.klejczyk.tpm.workorder.domain.WorkOrderStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class WorkOrderServiceIT {

    private static final Actor MANAGER = new Actor("mgr-1", Role.MANAGER);
    private static final Actor TECHNICIAN = new Actor("tech-1", Role.TECHNICIAN);

    @Autowired
    private WorkOrderService service;

    @Test
    void walksFullLifecycleFromReportToClose() {
        WorkOrder reported = service.report("m-1", WorkOrderReason.BREAKDOWN, "op-1");
        String id = reported.id();

        service.assign(id, MANAGER, "tech-1");
        service.start(id, TECHNICIAN);
        service.resolve(id, TECHNICIAN, "Bearing replaced");
        WorkOrder closed = service.close(id, MANAGER);

        assertThat(closed.status()).isEqualTo(WorkOrderStatus.CLOSED);
        assertThat(service.byId(id).resolvedAt()).isNotNull();
        assertThat(service.byId(id).startedAt()).isNotNull();
    }
}
