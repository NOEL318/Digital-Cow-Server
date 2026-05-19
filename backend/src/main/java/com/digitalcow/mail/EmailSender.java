package com.digitalcow.mail;

/** Abstraccion sobre envio de email. Dos impls: SMTP real y dev-logging. */
public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
