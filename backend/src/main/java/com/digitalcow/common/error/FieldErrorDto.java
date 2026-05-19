package com.digitalcow.common.error;

/** DTO para un error de validacion de campo. */
public record FieldErrorDto(String field, String code, String message) {}
