package com.digitalcow.account.dto;

public record AccountResponse(Long id, String name, String slug, String status,
                              String plan, String defaultLocale) {}
