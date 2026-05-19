package com.digitalcow.user.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(@Size(max = 160) String fullName, Locale locale) {}
