package pl.klejczyk.tpm.workorder.domain.exception;

public class MissingResolution extends RuntimeException {

    public MissingResolution() {
        super("Resolving a work order requires a resolution description.");
    }
}