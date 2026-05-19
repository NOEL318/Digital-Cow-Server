package com.digitalcow.admin;

import com.digitalcow.user.AppUser;
import com.digitalcow.user.AppUserRepository;
import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Esta clase crea automaticamente un super administrador al primer arranque cuando
 * todavia no existe ninguno en la base de datos.
 *
 * El operador puede definir la password por la variable de entorno SUPERADMIN_PASSWORD
 * para tener control total. Si no la define, se genera una password aleatoria que se
 * imprime una sola vez en el log al nivel WARN. El operador debera leerla en ese
 * arranque inicial y rotarla en el primer login.
 *
 * No se vuelve a imprimir en arranques posteriores porque la condicion existsByRole
 * evita ejecutar este bloque cuando ya hay super administrador en base.
 */
@Component
public class SuperAdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrap.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String configuredPassword;

    public SuperAdminBootstrap(AppUserRepository users, PasswordEncoder encoder,
                               @Value("${digitalcow.superadmin.email}") String email,
                               @Value("${digitalcow.superadmin.password:}") String configuredPassword) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.configuredPassword = configuredPassword;
    }

    /**
     * Crea el super administrador si todavia no existe ninguno. Imprime la password
     * generada en el log unicamente cuando fue auto generada para que el operador
     * pueda usarla en el primer login.
     *
     * @param args argumentos de la linea de comandos, no usados
     */
    @Override
    public void run(String... args) {
        if (users.existsByRole(UserRole.SUPERADMIN)) return;
        boolean usedConfigured = configuredPassword != null && !configuredPassword.isBlank();
        String pwd = usedConfigured ? configuredPassword : generateRandomPassword();
        AppUser u = new AppUser();
        u.setAccountId(null);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(pwd));
        u.setFullName("Super Admin");
        u.setRole(UserRole.SUPERADMIN);
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerifiedAt(Instant.now());
        users.save(u);
        if (usedConfigured) {
            log.warn("SUPERADMIN CREATED - Email: {} Password tomada de SUPERADMIN_PASSWORD env. ROTATE AT FIRST LOGIN.", email);
        } else {
            log.warn("SUPERADMIN CREATED - Email: {} Password: {} ROTATE AT FIRST LOGIN", email, pwd);
        }
    }

    /** Construye una password aleatoria base64 url safe de 24 bytes de entropia. */
    private String generateRandomPassword() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
