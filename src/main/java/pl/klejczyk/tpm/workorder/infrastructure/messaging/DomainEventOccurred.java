package pl.klejczyk.tpm.workorder.infrastructure.messaging;

// internal
public record DomainEventOccurred(
        String routingKey,
        String type,
        Object payload,
        String correlationId) {
}
