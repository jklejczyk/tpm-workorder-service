package pl.klejczyk.tpm.workorder.api;

import jakarta.validation.constraints.NotBlank;

public record ResolveRequest(@NotBlank String resolution) {
}
