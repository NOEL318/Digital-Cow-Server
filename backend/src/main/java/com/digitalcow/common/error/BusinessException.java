package com.digitalcow.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepcion de dominio. Cada lanzamiento usa un ErrorCode tipado.
 * El GlobalExceptionHandler la traduce a respuesta HTTP.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public BusinessException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /** Este metodo arma una excepcion de recurso no encontrado. */
    public static BusinessException notFound(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.NOT_FOUND, message);
    }
    /** Este metodo arma una excepcion de conflicto de estado. */
    public static BusinessException conflict(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.CONFLICT, message);
    }
    /** Este metodo arma una excepcion de peticion invalida. */
    public static BusinessException badRequest(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.BAD_REQUEST, message);
    }
    /** Este metodo arma una excepcion de acceso prohibido. */
    public static BusinessException forbidden(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.FORBIDDEN, message);
    }
    /** Este metodo arma una excepcion de no autorizado. */
    public static BusinessException unauthorized(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.UNAUTHORIZED, message);
    }
}
