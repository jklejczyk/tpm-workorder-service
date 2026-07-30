package pl.klejczyk.tpm.workorder.domain.exception;

import pl.klejczyk.tpm.workorder.domain.WorkOrderStatus;

public class IllegalStateTransition extends RuntimeException {

    private IllegalStateTransition(String message) {
        super(message);
    }

    public static IllegalStateTransition from(WorkOrderStatus status, String action) {
        return new IllegalStateTransition("Cannot perform '" + action + "' on a work order in state '" + status + "'.");
    }
}