package pl.klejczyk.tpm.workorder.domain.exception;

public class WorkOrderNotFound extends RuntimeException {

    public WorkOrderNotFound(String id) {
        super("Work order not found: " + id);
    }
}
