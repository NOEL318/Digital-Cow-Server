package com.digitalcow.mail;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Envia email via Resend (POST https://api.resend.com/emails).
 * Activa cuando digitalcow.mail.provider=resend. La API key y el
 * dominio de "from" provienen exclusivamente de variables de entorno;
 * nada se versiona.
 */
@Component
@ConditionalOnProperty(name = "digitalcow.mail.provider", havingValue = "resend")
public class ResendEmailSender implements EmailSender {

    private static final String ENDPOINT = "https://api.resend.com/emails";

    private final RestClient client;
    private final String from;

    public ResendEmailSender(
            @Value("${digitalcow.mail.resend.api-key}") String apiKey,
            @Value("${digitalcow.mail.from}") String from) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "RESEND_API_KEY no configurada. Defina la variable de entorno o cambie digitalcow.mail.provider.");
        }
        if (from == null || from.isBlank()) {
            throw new IllegalStateException(
                "digitalcow.mail.from sin valor. Defina MAIL_FROM (Nombre <email@dominio>) en el entorno.");
        }
        this.from = from;
        this.client = RestClient.builder()
            .baseUrl(ENDPOINT)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /** Este metodo envia el mensaje al destinatario indicado. */
    @Override
    @CircuitBreaker(name = "resend")
    @Retry(name = "resend")
    public void send(String to, String subject, String htmlBody) {
        Map<String, Object> payload = Map.of(
            "from", from,
            "to", new String[] { to },
            "subject", subject,
            "html", htmlBody
        );
        client.post()
            .body(payload)
            .retrieve()
            .toBodilessEntity();
    }
}
