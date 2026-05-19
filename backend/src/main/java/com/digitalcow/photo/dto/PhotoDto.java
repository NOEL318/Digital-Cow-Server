package com.digitalcow.photo.dto;

import java.time.Instant;

public record PhotoDto(Long id, String publicId, String url, Integer width, Integer height,
                       Integer bytes, Instant takenAt, Instant createdAt) {}
