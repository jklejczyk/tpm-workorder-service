package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.klejczyk.tpm.workorder.domain.KnownMachine;
import pl.klejczyk.tpm.workorder.domain.KnownMachineRepository;
import pl.klejczyk.tpm.workorder.support.CorrelationId;

import java.time.Clock;

@Component
class MachineEventListener {

    private static final Logger log = LoggerFactory.getLogger(MachineEventListener.class);

    private final KnownMachineRepository machines;
    private final ProcessedEventRepository processedEvents;
    private final Clock clock;

    MachineEventListener(KnownMachineRepository machines, ProcessedEventRepository processedEvents, Clock clock) {
        this.machines = machines;
        this.processedEvents = processedEvents;
        this.clock = clock;
    }

    @RabbitListener(queues = RabbitConfiguration.MACHINE_QUEUE)
    @Transactional
    void onMachineRegistered(EventEnvelope<MachineRegisteredEvent> envelope) {
        CorrelationId.set(envelope.correlationId());
        try {
            handle(envelope);
        } finally {
            CorrelationId.clear();
        }
    }

    private void handle(EventEnvelope<MachineRegisteredEvent> envelope) {
        if (processedEvents.existsById(envelope.eventId())) {
            log.info("Skipping duplicate eventId={}", envelope.eventId());
            return;
        }

        MachineRegisteredEvent payload = envelope.payload();
        machines.save(KnownMachine.of(payload.machineId(), payload.name()));
        processedEvents.save(new ProcessedEvent(envelope.eventId(), clock.instant()));

        log.info("Stored machine {}", payload.machineId());
    }
}
