package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pl.klejczyk.tpm.workorder.TestcontainersConfiguration;
import pl.klejczyk.tpm.workorder.domain.KnownMachineRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MachineEventListenerIT {

    @Autowired
    private MachineEventListener listener;

    @Autowired
    private KnownMachineRepository machines;

    @Test
    void processesTheSameEventOnlyOnce() {
        EventEnvelope<MachineRegisteredEvent> envelope = new EventEnvelope<>(
                "evt-1",
                "corr-1",
                "MachineRegistered",
                1,
                Instant.now(),
                new MachineRegisteredEvent("m-1", "Hydraulic press"));

        listener.onMachineRegistered(envelope);
        listener.onMachineRegistered(envelope);

        assertThat(machines.count()).isEqualTo(1);
    }
}
