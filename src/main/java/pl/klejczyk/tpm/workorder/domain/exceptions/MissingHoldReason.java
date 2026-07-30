package pl.klejczyk.tpm.workorder.domain.exception;

public class MissingHoldReason extends RuntimeException {

    public MissingHoldReason() {
        super("Putting a work order on hold requires a reason.");
    }
}