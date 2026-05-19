package com.digitalcow.common.error;

import java.util.List;
import java.util.Map;

/**
 * Shape estandar de error en respuestas HTTP.
 * Mapea 1:1 a seccion 4.5 del spec.
 */
public record ApiError(
    ErrorPayload error,
    String traceId
) {
    public record ErrorPayload(
        String code,
        String message,
        String messageKey,
        Map<String, Object> details,
        List<FieldErrorDto> fieldErrors
    ) {}
}
