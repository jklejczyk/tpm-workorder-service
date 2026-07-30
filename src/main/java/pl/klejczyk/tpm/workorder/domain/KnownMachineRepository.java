package pl.klejczyk.tpm.workorder.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnownMachineRepository extends JpaRepository<KnownMachine, String> {
}