package com.digitalcow.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(@NotBlank @Size(max = 160) String fullName,
                                       @NotBlank @Size(min = 8, max = 100) String password) {}
