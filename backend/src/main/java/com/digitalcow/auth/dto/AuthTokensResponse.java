package com.digitalcow.auth.dto;

public record AuthTokensResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
