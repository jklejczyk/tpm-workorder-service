package pl.klejczyk.tpm.workorder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "known_machines")
public class KnownMachine {

    @Id
    private String id;
    private String name;

    protected KnownMachine() {
    }

    private KnownMachine(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static KnownMachine of(String id, String name) {
        return new KnownMachine(id, name);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
