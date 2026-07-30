package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MachineRegisteredEvent(String machineId, String name) {
}