package pl.klejczyk.tpm.workorder.api;

import jakarta.validation.constraints.NotBlank;

public record AssignRequest(@NotBlank String technicianId) {
}
