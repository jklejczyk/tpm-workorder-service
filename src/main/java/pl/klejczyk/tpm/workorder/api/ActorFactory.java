package pl.klejczyk.tpm.workorder.api;

import pl.klejczyk.tpm.workorder.domain.Actor;
import pl.klejczyk.tpm.workorder.domain.Role;

final class ActorFactory {

    private ActorFactory() {
    }

    // TODO: implement real auth
    static Actor from(String actorId, String actorRole) {
        if (actorId == null || actorId.isBlank() || actorRole == null || actorRole.isBlank()) {
            throw new IllegalArgumentException("Headers X-Actor-Id and X-Actor-Role are required.");
        }
        return new Actor(actorId, Role.valueOf(actorRole));
    }
}