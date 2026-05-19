package com.digitalcow.photo.dto;

public record SignUploadResponse(String cloudName, String apiKey, long timestamp,
                                 String folder, String tags, String signature) {}
