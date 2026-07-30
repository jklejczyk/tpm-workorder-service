package pl.klejczyk.tpm.workorder.domain;

public record Actor(String id, Role role) {

    public boolean hasRole(Role expected) {
        return role == expected;
    }

    public boolean isAnyOf(Role... allowed) {
        for (Role candidate : allowed) {
            if (role == candidate) {
                return true;
            }
        }
        return false;
    }
}