package pl.klejczyk.tpm.workorder.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.klejczyk.tpm.workorder.domain.exception.WorkOrderNotFound;
import pl.klejczyk.tpm.workorder.domain.exception.IllegalStateTransition;
import pl.klejczyk.tpm.workorder.domain.exception.MissingHoldReason;
import pl.klejczyk.tpm.workorder.domain.exception.MissingResolution;
import pl.klejczyk.tpm.workorder.domain.exception.UnauthorizedTransition;

import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(WorkOrderNotFound.class)
    ResponseEntity<Map<String, String>> notFound(WorkOrderNotFound exception) {
        return status(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(UnauthorizedTransition.class)
    ResponseEntity<Map<String, String>> forbidden(UnauthorizedTransition exception) {
        return status(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler({IllegalStateTransition.class, MissingResolution.class, MissingHoldReason.class, IllegalArgumentException.class})
    ResponseEntity<Map<String, String>> unprocessable(RuntimeException exception) {
        return status(HttpStatus.UNPROCESSABLE_CONTENT, exception);
    }

    private ResponseEntity<Map<String, String>> status(HttpStatus code, RuntimeException e) {
        return ResponseEntity.status(code).body(Map.of("error", e.getMessage()));
    }
}
