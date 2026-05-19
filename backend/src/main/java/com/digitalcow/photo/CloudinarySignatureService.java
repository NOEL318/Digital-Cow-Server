package com.digitalcow.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Firma uploads a Cloudinary sin usar el SDK.
 * Algoritmo: concatena params ordenados alfabeticamente como key=value separados por &amp;,
 * append api_secret, SHA-1 hex.
 *
 * Valida al firmar que las tres variables de entorno de Cloudinary
 * esten presentes; si alguna esta vacia devuelve 503 con un mensaje
 * accionable en vez de generar una firma inservible que terminaria
 * en 401 confuso del lado de Cloudinary.
 */
@Service
public class CloudinarySignatureService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinarySignatureService(
        @Value("${digitalcow.cloudinary.cloud-name}") String cloudName,
        @Value("${digitalcow.cloudinary.api-key}") String apiKey,
        @Value("${digitalcow.cloudinary.api-secret}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /** Devuelve los parametros necesarios + signature lista para FormData. */
    public SignedUpload sign(Long accountId, Long animalId) {
        ensureConfigured();
        long ts = Instant.now().getEpochSecond();
        String folder = "accounts/" + accountId + "/animals/" + animalId;
        String tags = "animal-" + animalId;
        Map<String, String> params = new TreeMap<>();
        params.put("folder", folder);
        params.put("tags", tags);
        params.put("timestamp", String.valueOf(ts));
        String signature = signParams(params);
        return new SignedUpload(cloudName, apiKey, ts, folder, tags, signature);
    }

    /** Algoritmo de firma. Publico para testeo. */
    public String signParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(params).forEach((k, v) -> {
            if (sb.length() > 0) sb.append('&');
            sb.append(k).append('=').append(v);
        });
        sb.append(apiSecret);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void ensureConfigured() {
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Cloudinary no esta configurado. Define CLOUDINARY_CLOUD_NAME, "
                    + "CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET en el archivo .env "
                    + "y reinicia el backend.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public record SignedUpload(String cloudName, String apiKey, long timestamp,
                               String folder, String tags, String signature) {}
}
