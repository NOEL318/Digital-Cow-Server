package com.digitalcow.common.error;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Mapea excepciones a respuestas ApiError consistentes (spec seccion 4.8).
 * Todos los errores incluyen traceId del MDC.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Este metodo maneja las excepciones de negocio del dominio. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), null, null);
    }

    /** Este metodo maneja errores de validacion de los DTOs entrantes. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            "Validation failed", null, fields);
    }

    /** Este metodo maneja el caso de entidad JPA no encontrada. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), null, null);
    }

    /** Este metodo maneja el caso de acceso denegado por permisos. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, ex.getMessage(), null, null);
    }

    /** Este metodo maneja el caso de usuario no autenticado. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED, ex.getMessage(), null, null);
    }

    /** Este metodo maneja violaciones de integridad como duplicados o nulls invalidos. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        ErrorCode code = ErrorCode.CONFLICT;
        if (msg != null && msg.contains("uq_animal_tag")) code = ErrorCode.ANIMAL_TAG_DUPLICATE;
        else if (msg != null && msg.contains("uq_animal_official_tag")) code = ErrorCode.ANIMAL_OFFICIAL_TAG_DUPLICATE;
        else if (msg != null && msg.contains("uq_user_email")) code = ErrorCode.AUTH_EMAIL_ALREADY_USED;
        return build(HttpStatus.CONFLICT, code, "Conflict", null, null);
    }

    /** Este metodo maneja cualquier excepcion no contemplada y devuelve un error 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
            "Internal error", null, null);
    }

    private FieldErrorDto toFieldError(FieldError fe) {
        return new FieldErrorDto(fe.getField(), fe.getCode(), fe.getDefaultMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, ErrorCode code, String msg,
                                           Map<String, Object> details, List<FieldErrorDto> fields) {
        String traceId = MDC.get("traceId");
        ApiError body = new ApiError(
            new ApiError.ErrorPayload(code.name(), msg, code.messageKey(), details, fields),
            traceId
        );
        return ResponseEntity.status(status).body(body);
    }
}
