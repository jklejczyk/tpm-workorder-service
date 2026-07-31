package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.klejczyk.tpm.workorder.support.CorrelationId;

import java.time.Clock;
import java.util.UUID;

@Component
class RabbitEventBridge {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventBridge.class);

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    RabbitEventBridge(RabbitTemplate rabbitTemplate, Clock clock) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void send(DomainEventOccurred event) {
        String correlationId = CorrelationId.current();

        EventEnvelope<Object> envelope = new EventEnvelope<>(UUID.randomUUID().toString(), correlationId != null ? correlationId : UUID.randomUUID().toString(), event.type(), 1, clock.instant(), event.payload());

        try {
            rabbitTemplate.convertAndSend(RabbitConfiguration.EXCHANGE, event.routingKey(), envelope);
            log.info("Published {} eventId={}", envelope.type(), envelope.eventId());
        } catch (Exception exception) {
            log.error("Failed to publish {} eventId={} - event lost", envelope.type(), envelope.eventId(), exception);
        }
    }
}
