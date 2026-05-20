package com.digitalcow.animal.dto;

/**
 * Respuesta del endpoint que provee el token publico de un animal.
 * El frontend lo usa para construir el enlace de WhatsApp.
 */
public record AnimalShareTokenResponse(String shareToken) {}
