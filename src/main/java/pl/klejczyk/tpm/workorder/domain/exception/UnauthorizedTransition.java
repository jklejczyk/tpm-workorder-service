package pl.klejczyk.tpm.workorder.domain.exception;

import pl.klejczyk.tpm.workorder.domain.Actor;

public class UnauthorizedTransition extends RuntimeException {

    private UnauthorizedTransition(String message) {
        super(message);
    }

    public static UnauthorizedTransition forActor(Actor actor, String action) {
        return new UnauthorizedTransition("Role " + actor.role() + " is not allowed to perform '" + action + "'.");
    }
}