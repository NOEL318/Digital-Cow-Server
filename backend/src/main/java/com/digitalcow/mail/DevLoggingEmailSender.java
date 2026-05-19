package com.digitalcow.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * En dev: loguea el email en consola en vez de mandarlo. Es el
 * proveedor por defecto si digitalcow.mail.provider no esta seteado.
 */
@Component
@ConditionalOnProperty(name = "digitalcow.mail.provider", havingValue = "dev", matchIfMissing = true)
public class DevLoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DevLoggingEmailSender.class);

    /** Este metodo envia el mensaje al destinatario indicado. */
    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("\n--- DEV EMAIL ---\nTo: {}\nSubject: {}\nBody:\n{}\n-----------------",
            to, subject, htmlBody);
    }
}
