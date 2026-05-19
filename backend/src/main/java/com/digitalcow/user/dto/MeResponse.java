package com.digitalcow.user.dto;

import com.digitalcow.account.Locale;
import com.digitalcow.user.UserRole;

public record MeResponse(Long id, Long accountId, String email, String fullName,
                         UserRole role, Locale locale, boolean emailVerified) {}
