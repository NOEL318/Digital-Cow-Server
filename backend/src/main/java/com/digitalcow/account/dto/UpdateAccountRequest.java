package com.digitalcow.account.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(@Size(max = 120) String name, Locale defaultLocale) {}
