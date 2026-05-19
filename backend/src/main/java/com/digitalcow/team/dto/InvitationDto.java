package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;

import java.time.Instant;

public record InvitationDto(Long id, String email, UserRole role,
                            Instant expiresAt, Instant acceptedAt) {}
