package com.digitalcow.auth.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(max = 120) String accountName,
    @NotBlank @Size(max = 160) String fullName,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    Locale locale
) {}
