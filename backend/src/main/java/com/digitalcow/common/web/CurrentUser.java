package com.digitalcow.common.web;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;

/** Helper estatico para obtener el principal autenticado actual. */
public final class CurrentUser {

    private CurrentUser() {}

    /** Este metodo devuelve el principal autenticado o lanza si no hay sesion. */
    public static AuthPrincipal require() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal p)) {
            throw BusinessException.unauthorized(ErrorCode.UNAUTHENTICATED, "No principal");
        }
        return p;
    }
}
