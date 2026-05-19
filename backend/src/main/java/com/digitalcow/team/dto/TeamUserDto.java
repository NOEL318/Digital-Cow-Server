package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;

public record TeamUserDto(Long id, String email, String fullName,
                          UserRole role, UserStatus status) {}
