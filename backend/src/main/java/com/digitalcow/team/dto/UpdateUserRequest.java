package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;

public record UpdateUserRequest(UserRole role, UserStatus status) {}
