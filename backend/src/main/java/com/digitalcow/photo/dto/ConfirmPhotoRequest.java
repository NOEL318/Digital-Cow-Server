package com.digitalcow.photo.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record ConfirmPhotoRequest(
    @NotBlank String publicId,
    @NotBlank String url,
    Integer width,
    Integer height,
    Integer bytes,
    Instant takenAt
) {}
