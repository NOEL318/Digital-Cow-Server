package com.digitalcow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point del backend Digital Cow.
 * Habilita caching (Caffeine para dashboard) y scheduling (jobs futuros).
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class DigitalCowApplication {

    /**
     * Lanza la aplicacion Spring Boot.
     *
     * @param args argumentos de linea de comandos pasados a Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(DigitalCowApplication.class, args);
    }
}
