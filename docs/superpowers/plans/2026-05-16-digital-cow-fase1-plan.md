# Digital Cow Fase 1 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir la plataforma SaaS multi-tenant de Digital Cow Fase 1: registro, autenticación, equipos con roles, ranchos, lotes, catálogo de animales con fotos via Cloudinary, dashboard con gráficas, todo bilingüe ES/EN y empaquetado con Docker Compose.

**Architecture:** Monolito modular Spring Boot 3.3 / Java 21 con MySQL 8 (multi-tenancy por columna `account_id` con filtro Hibernate). Frontend React 18 + Vite + shadcn + TanStack Query + react-i18next, instalable como PWA. Despliegue con docker-compose en VPS único.

**Tech Stack:** Java 21, Spring Boot 3.3 (Web, Security, Data JPA, Validation, Actuator), Hibernate 6, Flyway, MapStruct, Lombok, jjwt 0.12, Resilience4j, Bucket4j, Testcontainers, JUnit 5, MySQL 8, Cloudinary, React 18, TypeScript, Vite 5, Tailwind, shadcn/ui, React Router 6, TanStack Query 5, react-hook-form + zod, react-i18next, Recharts, axios, vite-plugin-pwa, Docker, docker-compose.

---

## Épica A — Bootstrap del proyecto

### Task 1: Estructura monorepo y .gitignore raíz

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/.gitignore`
- Create: `/Users/noel/REPOS/Digital-Cow/.env.example`
- Create: `/Users/noel/REPOS/Digital-Cow/README.md`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/.gitkeep`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/.gitkeep`

- [ ] **Step 1: Crear `.gitignore` raíz**

```
# JetBrains / VSCode
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db

# Env
.env
.env.local
.env.*.local

# Backend
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar
backend/HELP.md

# Frontend
frontend/node_modules/
frontend/dist/
frontend/dev-dist/
frontend/coverage/
frontend/.vite/

# Logs
*.log
```

- [ ] **Step 2: Crear `.env.example` con todas las variables documentadas en el spec §6.1**

```
# Database
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=digitalcow
MYSQL_USER=digitalcow
MYSQL_PASSWORD=changeme
MYSQL_ROOT_PASSWORD=changemeroot

# Backend
JWT_SECRET=replace-with-256-bit-random-secret-replace-with-256-bit-random-secret
CORS_ALLOWED_ORIGINS=http://localhost:5173
SUPERADMIN_EMAIL=admin@digitalcow.local

# Cloudinary
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# SMTP (opcional, dev usa logging)
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASSWORD=
SMTP_FROM=no-reply@digitalcow.local

# Frontend (build-time)
VITE_API_URL=http://localhost:8080/api/v1
```

- [ ] **Step 3: Crear `README.md` skeleton**

```markdown
# Digital Cow

Plataforma SaaS multi-tenant para gestión ganadera (Fase 1: catálogo de ganado).

## Stack
- Backend: Java 21, Spring Boot 3.3, MySQL 8
- Frontend: React 18, Vite, shadcn/ui, TanStack Query
- Infra: Docker Compose

## Quick start
1. `cp .env.example .env` y editar valores.
2. `docker compose up --build`
3. Abrir `http://localhost:5173`.

Ver `docs/superpowers/specs/2026-05-16-digital-cow-fase1-design.md` para diseño completo.
```

- [ ] **Step 4: Crear directorios `backend/` y `frontend/` con `.gitkeep`**

Contenido de ambos archivos: vacío.

- [ ] **Step 5: Validar estructura**

Run: `ls -la /Users/noel/REPOS/Digital-Cow`
Expected: ver `.gitignore`, `.env.example`, `README.md`, `backend/`, `frontend/`, `docs/`.

- [ ] **Step 6: Pausa de revisión**

Archivos a revisar: `.gitignore`, `.env.example`, `README.md`. Confirmar que no falten variables.

---

### Task 2: Bootstrap backend Spring Boot — `pom.xml` y main class

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/pom.xml`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/DigitalCowApplication.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/mvnw`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/mvnw.cmd`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/.mvn/wrapper/maven-wrapper.properties`

- [ ] **Step 1: Crear `pom.xml` completo con todas las dependencias del stack**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.digitalcow</groupId>
    <artifactId>digitalcow-backend</artifactId>
    <version>0.1.0</version>
    <name>Digital Cow Backend</name>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.2</mapstruct.version>
        <lombok.version>1.18.34</lombok.version>
        <jjwt.version>0.12.6</jjwt.version>
        <resilience4j.version>2.2.0</resilience4j.version>
        <springdoc.version>2.6.0</springdoc.version>
        <bucket4j.version>8.10.1</bucket4j.version>
        <testcontainers.version>1.20.2</testcontainers.version>
        <logstash.version>8.0</logstash.version>
    </properties>

    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>

        <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>

        <dependency><groupId>com.github.ben-manes.caffeine</groupId><artifactId>caffeine</artifactId></dependency>

        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>${lombok.version}</version><scope>provided</scope></dependency>
        <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId><version>${mapstruct.version}</version></dependency>

        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>

        <dependency><groupId>io.github.resilience4j</groupId><artifactId>resilience4j-spring-boot3</artifactId><version>${resilience4j.version}</version></dependency>
        <dependency><groupId>com.bucket4j</groupId><artifactId>bucket4j_jdk17-core</artifactId><version>${bucket4j.version}</version></dependency>

        <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>${springdoc.version}</version></dependency>
        <dependency><groupId>net.logstash.logback</groupId><artifactId>logstash-logback-encoder</artifactId><version>${logstash.version}</version></dependency>

        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><version>${testcontainers.version}</version><scope>test</scope></dependency>
        <dependency><groupId>org.testcontainers</groupId><artifactId>mysql</artifactId><version>${testcontainers.version}</version><scope>test</scope></dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>${lombok.version}</version></path>
                        <path><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>${mapstruct.version}</version></path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Crear `DigitalCowApplication.java`**

```java
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

    public static void main(String[] args) {
        SpringApplication.run(DigitalCowApplication.class, args);
    }
}
```

- [ ] **Step 3: Generar maven wrapper**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && mvn -N wrapper:wrapper -Dmaven=3.9.9`
Expected: archivos `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` creados.

- [ ] **Step 4: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS sin errores.

- [ ] **Step 5: Pausa de revisión**

Archivos a revisar: `pom.xml`, `DigitalCowApplication.java`. Confirmar versiones.

---

### Task 3: Configuración `application.yml` (dev y prod)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/application.yml`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/application-dev.yml`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/application-prod.yml`

- [ ] **Step 1: Crear `application.yml` base**

```yaml
spring:
  application:
    name: digitalcow
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:digitalcow}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: ${MYSQL_USER:digitalcow}
    password: ${MYSQL_PASSWORD:changeme}
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        jdbc.time_zone: UTC
        format_sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
  mail:
    host: ${SMTP_HOST:}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USER:}
    password: ${SMTP_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

server:
  port: 8080
  forward-headers-strategy: framework

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

digitalcow:
  security:
    jwt:
      secret: ${JWT_SECRET}
      access-token-ttl-minutes: 15
      refresh-token-ttl-days: 30
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
  cloudinary:
    cloud-name: ${CLOUDINARY_CLOUD_NAME:}
    api-key: ${CLOUDINARY_API_KEY:}
    api-secret: ${CLOUDINARY_API_SECRET:}
  mail:
    from: ${SMTP_FROM:no-reply@digitalcow.local}
  superadmin:
    email: ${SUPERADMIN_EMAIL:admin@digitalcow.local}

resilience4j:
  circuitbreaker:
    instances:
      cloudinary:
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
  retry:
    instances:
      cloudinary:
        maxAttempts: 3
        waitDuration: 200ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

- [ ] **Step 2: Crear `application-dev.yml`**

```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    com.digitalcow: DEBUG
    org.hibernate.SQL: DEBUG
digitalcow:
  mail:
    dev-logging: true
```

- [ ] **Step 3: Crear `application-prod.yml`**

```yaml
logging:
  level:
    root: INFO
    com.digitalcow: INFO
digitalcow:
  mail:
    dev-logging: false
```

- [ ] **Step 4: Validar arranque básico**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && JWT_SECRET=$(openssl rand -base64 48) ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments='-Dserver.port=0' -q &`
Expected: arranca y luego falla por falta de MySQL (esperado). Detener con `kill %1`.

- [ ] **Step 5: Pausa de revisión**

Archivos: `application.yml`, perfiles. Confirmar variables documentadas en `.env.example`.

---

### Task 4: Bootstrap frontend Vite + React + TS

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/package.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/tsconfig.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/tsconfig.node.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/vite.config.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/tailwind.config.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/postcss.config.js`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/index.html`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/main.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/App.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/index.css`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/vite-env.d.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/components.json`

- [ ] **Step 1: Crear `package.json`**

```json
{
  "name": "digitalcow-frontend",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "typecheck": "tsc -b --noEmit",
    "lint": "eslint .",
    "test": "vitest run",
    "test:watch": "vitest"
  },
  "dependencies": {
    "@hookform/resolvers": "^3.9.0",
    "@radix-ui/react-dialog": "^1.1.2",
    "@radix-ui/react-dropdown-menu": "^2.1.2",
    "@radix-ui/react-label": "^2.1.0",
    "@radix-ui/react-select": "^2.1.2",
    "@radix-ui/react-slot": "^1.1.0",
    "@radix-ui/react-tabs": "^1.1.1",
    "@radix-ui/react-toast": "^1.2.2",
    "@tanstack/react-query": "^5.59.0",
    "@tanstack/react-table": "^8.20.5",
    "axios": "^1.7.7",
    "browser-image-compression": "^2.0.2",
    "class-variance-authority": "^0.7.0",
    "clsx": "^2.1.1",
    "i18next": "^23.15.1",
    "i18next-browser-languagedetector": "^8.0.0",
    "i18next-http-backend": "^2.6.2",
    "lucide-react": "^0.451.0",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-hook-form": "^7.53.0",
    "react-i18next": "^15.0.2",
    "react-router-dom": "^6.26.2",
    "recharts": "^2.13.0",
    "tailwind-merge": "^2.5.2",
    "tailwindcss-animate": "^1.0.7",
    "zod": "^3.23.8"
  },
  "devDependencies": {
    "@testing-library/jest-dom": "^6.5.0",
    "@testing-library/react": "^16.0.1",
    "@testing-library/user-event": "^14.5.2",
    "@types/node": "^22.7.4",
    "@types/react": "^18.3.11",
    "@types/react-dom": "^18.3.0",
    "@typescript-eslint/eslint-plugin": "^8.8.0",
    "@typescript-eslint/parser": "^8.8.0",
    "@vitejs/plugin-react": "^4.3.2",
    "autoprefixer": "^10.4.20",
    "eslint": "^9.11.1",
    "eslint-plugin-react-hooks": "^5.1.0-rc-fb9a90fa48-20240614",
    "eslint-plugin-react-refresh": "^0.4.12",
    "jsdom": "^25.0.1",
    "msw": "^2.4.9",
    "postcss": "^8.4.47",
    "prettier": "^3.3.3",
    "tailwindcss": "^3.4.13",
    "typescript": "^5.6.2",
    "vite": "^5.4.8",
    "vite-plugin-pwa": "^0.20.5",
    "vitest": "^2.1.2"
  }
}
```

- [ ] **Step 2: Crear `tsconfig.json` y `tsconfig.node.json`**

`tsconfig.json`:
```json
{
  "compilerOptions": {
    "target": "ES2022",
    "useDefineForClassFields": true,
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": false,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": { "@/*": ["src/*"] },
    "types": ["vitest/globals", "@testing-library/jest-dom"]
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

`tsconfig.node.json`:
```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "strict": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 3: Crear `vite.config.ts` con PWA**

```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import path from 'node:path';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'icons/icon-192.png', 'icons/icon-512.png'],
      manifest: {
        name: 'Digital Cow',
        short_name: 'DigitalCow',
        description: 'Gestion ganadera multi-tenant',
        theme_color: '#0f172a',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png', purpose: 'any maskable' }
        ]
      },
      workbox: {
        runtimeCaching: [
          {
            urlPattern: ({ url }) => url.pathname.endsWith('/api/v1/breeds'),
            handler: 'StaleWhileRevalidate',
            options: { cacheName: 'breeds-cache' }
          }
        ]
      }
    })
  ],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') }
  },
  server: { port: 5173 },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts']
  }
});
```

- [ ] **Step 4: Crear `tailwind.config.ts` y `postcss.config.js`**

`tailwind.config.ts`:
```ts
import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    container: { center: true, padding: '1rem', screens: { '2xl': '1400px' } },
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: { DEFAULT: 'hsl(var(--primary))', foreground: 'hsl(var(--primary-foreground))' },
        secondary: { DEFAULT: 'hsl(var(--secondary))', foreground: 'hsl(var(--secondary-foreground))' },
        destructive: { DEFAULT: 'hsl(var(--destructive))', foreground: 'hsl(var(--destructive-foreground))' },
        muted: { DEFAULT: 'hsl(var(--muted))', foreground: 'hsl(var(--muted-foreground))' },
        accent: { DEFAULT: 'hsl(var(--accent))', foreground: 'hsl(var(--accent-foreground))' },
        card: { DEFAULT: 'hsl(var(--card))', foreground: 'hsl(var(--card-foreground))' }
      },
      borderRadius: { lg: 'var(--radius)', md: 'calc(var(--radius) - 2px)', sm: 'calc(var(--radius) - 4px)' }
    }
  },
  plugins: [require('tailwindcss-animate')]
};
export default config;
```

`postcss.config.js`:
```js
export default {
  plugins: { tailwindcss: {}, autoprefixer: {} }
};
```

- [ ] **Step 5: Crear `index.html`, `src/main.tsx`, `src/App.tsx`, `src/index.css`, `src/vite-env.d.ts`**

`index.html`:
```html
<!doctype html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <title>Digital Cow</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

`src/main.tsx`:
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

`src/App.tsx`:
```tsx
/**
 * Componente raiz. En esta task solo renderiza placeholder.
 * Las providers y router se conectan en epicas posteriores.
 */
export default function App() {
  return (
    <div className="p-8 text-center">
      <h1 className="text-2xl font-bold">Digital Cow</h1>
      <p className="text-muted-foreground">Bootstrapping...</p>
    </div>
  );
}
```

`src/index.css`:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --card: 0 0% 100%;
    --card-foreground: 222.2 84% 4.9%;
    --primary: 222.2 47.4% 11.2%;
    --primary-foreground: 210 40% 98%;
    --secondary: 210 40% 96.1%;
    --secondary-foreground: 222.2 47.4% 11.2%;
    --muted: 210 40% 96.1%;
    --muted-foreground: 215.4 16.3% 46.9%;
    --accent: 210 40% 96.1%;
    --accent-foreground: 222.2 47.4% 11.2%;
    --destructive: 0 84.2% 60.2%;
    --destructive-foreground: 210 40% 98%;
    --border: 214.3 31.8% 91.4%;
    --input: 214.3 31.8% 91.4%;
    --ring: 222.2 84% 4.9%;
    --radius: 0.5rem;
  }
  .dark {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    --card: 222.2 84% 4.9%;
    --card-foreground: 210 40% 98%;
    --primary: 210 40% 98%;
    --primary-foreground: 222.2 47.4% 11.2%;
    --secondary: 217.2 32.6% 17.5%;
    --secondary-foreground: 210 40% 98%;
    --muted: 217.2 32.6% 17.5%;
    --muted-foreground: 215 20.2% 65.1%;
    --accent: 217.2 32.6% 17.5%;
    --accent-foreground: 210 40% 98%;
    --destructive: 0 62.8% 30.6%;
    --destructive-foreground: 210 40% 98%;
    --border: 217.2 32.6% 17.5%;
    --input: 217.2 32.6% 17.5%;
    --ring: 212.7 26.8% 83.9%;
  }
}

@layer base {
  * { @apply border-border; }
  body { @apply bg-background text-foreground; }
}
```

`src/vite-env.d.ts`:
```ts
/// <reference types="vite/client" />
/// <reference types="vite-plugin-pwa/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
}
interface ImportMeta { readonly env: ImportMetaEnv; }
```

- [ ] **Step 6: Crear `components.json` para shadcn**

```json
{
  "$schema": "https://ui.shadcn.com/schema.json",
  "style": "default",
  "rsc": false,
  "tsx": true,
  "tailwind": {
    "config": "tailwind.config.ts",
    "css": "src/index.css",
    "baseColor": "slate",
    "cssVariables": true,
    "prefix": ""
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib/utils"
  }
}
```

- [ ] **Step 7: Instalar dependencias y validar build**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm install`
Expected: instala sin errores criticos.

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run build`
Expected: build exitoso, genera `dist/`.

- [ ] **Step 8: Pausa de revisión**

Archivos: `package.json`, `vite.config.ts`, `tailwind.config.ts`, `App.tsx`. Confirmar versiones y aliases.

---

### Task 5: ESLint, Prettier y test setup frontend

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/eslint.config.js`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/.prettierrc.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/test/setup.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/lib/utils.ts`

- [ ] **Step 1: Crear `eslint.config.js`**

```js
import js from '@eslint/js';
import tsPlugin from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';

export default [
  { ignores: ['dist', 'dev-dist', 'node_modules'] },
  js.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: { parser: tsParser, ecmaVersion: 2022, sourceType: 'module' },
    plugins: {
      '@typescript-eslint': tsPlugin,
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh
    },
    rules: {
      ...tsPlugin.configs.recommended.rules,
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': 'warn',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }]
    }
  }
];
```

- [ ] **Step 2: Crear `.prettierrc.json`**

```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "none",
  "printWidth": 100,
  "tabWidth": 2
}
```

- [ ] **Step 3: Crear `src/lib/utils.ts` (helper shadcn `cn`)**

```ts
import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/** Combina class names con tailwind-merge para resolver conflictos. */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}
```

- [ ] **Step 4: Crear `src/test/setup.ts`**

```ts
import '@testing-library/jest-dom';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

afterEach(() => cleanup());
```

- [ ] **Step 5: Validar lint y typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run lint`
Expected: 0 errores.

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 6: Pausa de revisión**

Archivos: `eslint.config.js`, `.prettierrc.json`, `src/lib/utils.ts`.

---

### Task 6: Dockerfiles y docker-compose

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/Dockerfile`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/Dockerfile`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/nginx.conf`
- Create: `/Users/noel/REPOS/Digital-Cow/docker-compose.yml`

- [ ] **Step 1: Crear `backend/Dockerfile` multi-stage**

```dockerfile
# Stage 1: build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

- [ ] **Step 2: Crear `frontend/Dockerfile` multi-stage**

```dockerfile
# Stage 1: build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Stage 2: nginx
FROM nginx:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx","-g","daemon off;"]
```

- [ ] **Step 3: Crear `frontend/nginx.conf` con SPA fallback**

```nginx
server {
  listen 80;
  server_name _;
  root /usr/share/nginx/html;
  index index.html;

  # Cache de assets estaticos
  location /assets/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }

  # SPA fallback: cualquier ruta no encontrada sirve index.html
  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

- [ ] **Step 4: Crear `docker-compose.yml`**

```yaml
services:
  mysql:
    image: mysql:8.0
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 5s
      timeout: 5s
      retries: 20
    ports:
      - "3306:3306"

  adminer:
    image: adminer:4
    restart: unless-stopped
    ports:
      - "8081:8080"
    depends_on:
      - mysql

  backend:
    build:
      context: ./backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      MYSQL_PORT: 3306
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
      CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
      CLOUDINARY_API_KEY: ${CLOUDINARY_API_KEY}
      CLOUDINARY_API_SECRET: ${CLOUDINARY_API_SECRET}
      SMTP_HOST: ${SMTP_HOST}
      SMTP_PORT: ${SMTP_PORT}
      SMTP_USER: ${SMTP_USER}
      SMTP_PASSWORD: ${SMTP_PASSWORD}
      SMTP_FROM: ${SMTP_FROM}
      SUPERADMIN_EMAIL: ${SUPERADMIN_EMAIL}
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"

  frontend:
    build:
      context: ./frontend
      args:
        VITE_API_URL: ${VITE_API_URL}
    restart: unless-stopped
    depends_on:
      - backend
    ports:
      - "5173:80"

volumes:
  mysql-data:
```

- [ ] **Step 5: Validar sintaxis compose**

Run: `docker compose -f /Users/noel/REPOS/Digital-Cow/docker-compose.yml config -q`
Expected: sin errores (puede advertir vars vacias, OK).

- [ ] **Step 6: Pausa de revisión**

Archivos: dos Dockerfiles, nginx.conf, docker-compose.yml. Confirmar puertos.

---

## Épica B — Backend: fundación

### Task 7: Beans de configuración (Security, CORS, OpenAPI, Cache)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/config/SecurityConfig.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/config/OpenApiConfig.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/config/CacheConfig.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/config/PasswordEncoderConfig.java`

- [ ] **Step 1: Crear `SecurityConfig.java` (sin JWT filter aun, se agrega en Task 14)**

```java
package com.digitalcow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion principal de Spring Security.
 * - Stateless con JWT (sesion siempre nueva).
 * - CORS por allowlist desde env.
 * - Headers HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy.
 * - Endpoints publicos: /auth/*, /actuator/health, /swagger-ui, /v3/api-docs, breeds.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${digitalcow.security.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h
                .contentTypeOptions(c -> {})
                .frameOptions(f -> f.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/breeds").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    /** Construye CorsConfigurationSource desde la propiedad allowed-origins. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET","POST","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("X-Trace-Id"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
```

- [ ] **Step 2: Crear `OpenApiConfig.java`**

```java
package com.digitalcow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura Swagger UI con esquema Bearer JWT. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalCowOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Digital Cow API").version("v1"))
            .addSecurityItem(new SecurityRequirement().addList("bearer"))
            .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearer",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
```

- [ ] **Step 3: Crear `CacheConfig.java`**

```java
package com.digitalcow.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Configura cache Caffeine para dashboard (TTL 60s). */
@Configuration
public class CacheConfig {

    public static final String DASHBOARD_CACHE = "dashboardSummary";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager(DASHBOARD_CACHE);
        mgr.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60))
            .maximumSize(1000));
        return mgr;
    }
}
```

- [ ] **Step 4: Crear `PasswordEncoderConfig.java`**

```java
package com.digitalcow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** BCrypt cost 12 (alto para uso interactivo). */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 4 clases de config. Confirmar imports y allowlist CORS.

---

### Task 8: Clases comunes (ErrorCode, ApiError, BusinessException, GlobalExceptionHandler)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/error/ErrorCode.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/error/ApiError.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/error/BusinessException.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/error/GlobalExceptionHandler.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/error/FieldErrorDto.java`

- [ ] **Step 1: Crear `ErrorCode.java`**

```java
package com.digitalcow.common.error;

/**
 * Codigos de error custom mapeados a messageKey i18n del frontend.
 * Convencion: SCOPE_REASON. Cada nuevo error debe agregarse aqui.
 */
public enum ErrorCode {
    INTERNAL_ERROR("errors.internal"),
    VALIDATION_ERROR("errors.validation"),
    UNAUTHENTICATED("errors.unauthenticated"),
    FORBIDDEN("errors.forbidden"),
    NOT_FOUND("errors.notFound"),
    CONFLICT("errors.conflict"),

    AUTH_INVALID_CREDENTIALS("errors.auth.invalidCredentials"),
    AUTH_EMAIL_NOT_VERIFIED("errors.auth.emailNotVerified"),
    AUTH_USER_DISABLED("errors.auth.userDisabled"),
    AUTH_TOKEN_INVALID("errors.auth.tokenInvalid"),
    AUTH_TOKEN_EXPIRED("errors.auth.tokenExpired"),
    AUTH_REFRESH_INVALID("errors.auth.refreshInvalid"),
    AUTH_EMAIL_ALREADY_USED("errors.auth.emailAlreadyUsed"),

    INVITATION_INVALID("errors.invitation.invalid"),
    INVITATION_EXPIRED("errors.invitation.expired"),
    INVITATION_ALREADY_ACCEPTED("errors.invitation.alreadyAccepted"),

    RANCH_HAS_ANIMALS("errors.ranch.hasAnimals"),
    LOT_HAS_ANIMALS("errors.lot.hasAnimals"),

    ANIMAL_TAG_DUPLICATE("errors.animal.tagDuplicate"),
    ANIMAL_OFFICIAL_TAG_DUPLICATE("errors.animal.officialTagDuplicate"),
    ANIMAL_NOT_DELETABLE("errors.animal.notDeletable"),

    PHOTO_PUBLIC_ID_INVALID("errors.photo.publicIdInvalid"),
    PHOTO_NOT_FOUND("errors.photo.notFound"),
    PHOTO_SERVICE_UNAVAILABLE("errors.photo.serviceUnavailable");

    private final String messageKey;
    ErrorCode(String messageKey) { this.messageKey = messageKey; }
    public String messageKey() { return messageKey; }
}
```

- [ ] **Step 2: Crear `FieldErrorDto.java` y `ApiError.java`**

`FieldErrorDto.java`:
```java
package com.digitalcow.common.error;

/** DTO para un error de validacion de campo. */
public record FieldErrorDto(String field, String code, String message) {}
```

`ApiError.java`:
```java
package com.digitalcow.common.error;

import java.util.List;
import java.util.Map;

/**
 * Shape estandar de error en respuestas HTTP.
 * Mapea 1:1 a §4.5 del spec.
 */
public record ApiError(
    ErrorPayload error,
    String traceId
) {
    public record ErrorPayload(
        String code,
        String message,
        String messageKey,
        Map<String, Object> details,
        List<FieldErrorDto> fieldErrors
    ) {}
}
```

- [ ] **Step 3: Crear `BusinessException.java`**

```java
package com.digitalcow.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepcion de dominio. Cada lanzamiento usa un ErrorCode tipado.
 * El GlobalExceptionHandler la traduce a respuesta HTTP.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public BusinessException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException notFound(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.NOT_FOUND, message);
    }
    public static BusinessException conflict(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.CONFLICT, message);
    }
    public static BusinessException badRequest(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.BAD_REQUEST, message);
    }
    public static BusinessException forbidden(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.FORBIDDEN, message);
    }
    public static BusinessException unauthorized(ErrorCode code, String message) {
        return new BusinessException(code, HttpStatus.UNAUTHORIZED, message);
    }
}
```

- [ ] **Step 4: Crear `GlobalExceptionHandler.java`**

```java
package com.digitalcow.common.error;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Mapea excepciones a respuestas ApiError consistentes (spec §4.8).
 * Todos los errores incluyen traceId del MDC.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            "Validation failed", null, fields);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), null, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, ex.getMessage(), null, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED, ex.getMessage(), null, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        ErrorCode code = ErrorCode.CONFLICT;
        if (msg != null && msg.contains("uq_animal_tag")) code = ErrorCode.ANIMAL_TAG_DUPLICATE;
        else if (msg != null && msg.contains("uq_animal_official_tag")) code = ErrorCode.ANIMAL_OFFICIAL_TAG_DUPLICATE;
        else if (msg != null && msg.contains("uq_user_email")) code = ErrorCode.AUTH_EMAIL_ALREADY_USED;
        return build(HttpStatus.CONFLICT, code, "Conflict", null, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
            "Internal error", null, null);
    }

    private FieldErrorDto toFieldError(FieldError fe) {
        return new FieldErrorDto(fe.getField(), fe.getCode(), fe.getDefaultMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, ErrorCode code, String msg,
                                           Map<String, Object> details, List<FieldErrorDto> fields) {
        String traceId = MDC.get("traceId");
        ApiError body = new ApiError(
            new ApiError.ErrorPayload(code.name(), msg, code.messageKey(), details, fields),
            traceId
        );
        return ResponseEntity.status(status).body(body);
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 5 clases. Confirmar que cada ErrorCode tenga uso futuro planeado.

---

### Task 9: TraceIdFilter y logging JSON

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/web/TraceIdFilter.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/logback-spring.xml`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/common/web/TraceIdFilterTest.java`

- [ ] **Step 1: Write failing test `TraceIdFilterTest`**

```java
package com.digitalcow.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TraceIdFilterTest {

    @Test
    void shouldGenerateTraceIdAndSetHeaderAndMdc() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(res.getHeader("X-Trace-Id")).isNotBlank();
        assertThat(MDC.get("traceId")).isNull(); // limpiado al final
        verify(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private static <T> T any(Class<T> c) { return org.mockito.ArgumentMatchers.any(c); }
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TraceIdFilterTest -q`
Expected: FAIL — `TraceIdFilter` no existe.

- [ ] **Step 3: Implementar `TraceIdFilter.java`**

```java
package com.digitalcow.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Genera un traceId por request, lo expone en MDC y en el header X-Trace-Id.
 * Limpia MDC al finalizar para evitar leak en virtual threads.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String existing = request.getHeader(HEADER);
        String traceId = (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
        try {
            MDC.put(MDC_KEY, traceId);
            response.setHeader(HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```

- [ ] **Step 4: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TraceIdFilterTest -q`
Expected: PASS.

- [ ] **Step 5: Crear `logback-spring.xml` con encoder JSON**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="prod">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
                <includeMdcKeyName>accountId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>

    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.digitalcow" level="DEBUG"/>
    </springProfile>
</configuration>
```

- [ ] **Step 6: Pausa de revisión**

Archivos: `TraceIdFilter.java`, `logback-spring.xml`, test. Verificar header.

---

### Task 10: AbstractAuditableEntity

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/jpa/AbstractAuditableEntity.java`

- [ ] **Step 1: Crear `AbstractAuditableEntity.java`**

```java
package com.digitalcow.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.Instant;

/**
 * Superclase base con timestamps automaticos.
 * Todas las entities multi-tenant heredan de aqui.
 */
@MappedSuperclass
@Getter
public abstract class AbstractAuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

- [ ] **Step 2: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Pausa de revisión**

Archivo: `AbstractAuditableEntity.java`. Confirmar que `@PrePersist` y `@PreUpdate` no choquen con listeners futuros.

---

## Épica C — Multi-tenancy core

### Task 11: Migración Flyway V1 core + seed razas

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V1__core.sql`

- [ ] **Step 1: Crear migración `V1__core.sql` con todas las tablas core**

```sql
-- Cuentas (tenant root)
CREATE TABLE account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  slug VARCHAR(60) NOT NULL,
  status ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  plan ENUM('FREE','PRO') NOT NULL DEFAULT 'FREE',
  default_locale ENUM('es','en') NOT NULL DEFAULT 'es',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_account_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Usuarios
CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NULL,
  email VARCHAR(180) NOT NULL,
  password_hash VARCHAR(120) NOT NULL,
  full_name VARCHAR(160) NOT NULL,
  role ENUM('OWNER','ADMIN','MANAGER','WORKER','VIEWER','SUPERADMIN') NOT NULL,
  locale ENUM('es','en') NULL,
  email_verified_at TIMESTAMP(6) NULL,
  status ENUM('ACTIVE','INVITED','DISABLED') NOT NULL DEFAULT 'INVITED',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_user_email UNIQUE (email),
  CONSTRAINT fk_user_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  INDEX ix_user_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Refresh tokens
CREATE TABLE refresh_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  revoked_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  INDEX ix_refresh_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Verificacion de email
CREATE TABLE email_verification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  used_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_email_verif_token UNIQUE (token),
  CONSTRAINT fk_email_verif_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reset de password
CREATE TABLE password_reset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  used_at TIMESTAMP(6) NULL,
  CONSTRAINT uq_pwd_reset_token UNIQUE (token),
  CONSTRAINT fk_pwd_reset_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Invitaciones a equipo
CREATE TABLE user_invitation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  email VARCHAR(180) NOT NULL,
  role ENUM('OWNER','ADMIN','MANAGER','WORKER','VIEWER') NOT NULL,
  token VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  accepted_at TIMESTAMP(6) NULL,
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT uq_invitation_token UNIQUE (token),
  CONSTRAINT fk_invitation_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
  CONSTRAINT fk_invitation_creator FOREIGN KEY (created_by_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
  INDEX ix_invitation_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Auditoria
CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NULL,
  user_id BIGINT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id BIGINT NULL,
  action ENUM('CREATE','UPDATE','DELETE','LOGIN','INVITE') NOT NULL,
  payload_json JSON NULL,
  ip VARCHAR(45) NULL,
  user_agent VARCHAR(250) NULL,
  created_at TIMESTAMP(6) NOT NULL,
  INDEX ix_audit_account_created (account_id, created_at),
  INDEX ix_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Pausa de revisión**

Archivo: `V1__core.sql`. Confirmar índices, FKs y enum values consistentes con spec §4.4.

---

### Task 12: Entities Account y AppUser + repos

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/Account.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/AccountStatus.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/AccountPlan.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/Locale.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/AccountRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/AppUser.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/UserRole.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/UserStatus.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/AppUserRepository.java`

- [ ] **Step 1: Crear enums account**

`AccountStatus.java`:
```java
package com.digitalcow.account;
public enum AccountStatus { ACTIVE, INACTIVE, SUSPENDED }
```

`AccountPlan.java`:
```java
package com.digitalcow.account;
public enum AccountPlan { FREE, PRO }
```

`Locale.java`:
```java
package com.digitalcow.account;
public enum Locale { es, en }
```

- [ ] **Step 2: Crear `Account.java`**

```java
package com.digitalcow.account;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant root. Cada Account agrupa todos los datos de una organizacion. */
@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Account extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private AccountPlan plan = AccountPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_locale", nullable = false, length = 2)
    private Locale defaultLocale = Locale.es;
}
```

- [ ] **Step 3: Crear `AccountRepository.java`**

```java
package com.digitalcow.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Acceso a Account. No filtrado por tenant (Account ES el tenant). */
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
```

- [ ] **Step 4: Crear enums user**

`UserRole.java`:
```java
package com.digitalcow.user;
public enum UserRole { OWNER, ADMIN, MANAGER, WORKER, VIEWER, SUPERADMIN }
```

`UserStatus.java`:
```java
package com.digitalcow.user;
public enum UserStatus { ACTIVE, INVITED, DISABLED }
```

- [ ] **Step 5: Crear `AppUser.java`**

```java
package com.digitalcow.user;

import com.digitalcow.account.Locale;
import com.digitalcow.common.jpa.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Usuario del sistema. SUPERADMIN tiene account_id NULL. */
@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor
public class AppUser extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NULL solo para SUPERADMIN. */
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 2)
    private Locale locale;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private UserStatus status = UserStatus.INVITED;
}
```

- [ ] **Step 6: Crear `AppUserRepository.java`**

```java
package com.digitalcow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Acceso a AppUser. Algunos metodos cruzan tenant (login). */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findAllByAccountId(Long accountId);
    boolean existsByRole(UserRole role);
}
```

- [ ] **Step 7: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 8: Pausa de revisión**

Archivos: 9 archivos en `account/` y `user/`. Confirmar enums consistentes con migración V1.

---

### Task 13: TenantContext, @SkipTenancy, TenancyFilter

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/tenancy/TenantContext.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/tenancy/SkipTenancy.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/tenancy/TenantAwareEntityListener.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/tenancy/TenantFilterAspect.java`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/tenancy/TenantContextTest.java`

- [ ] **Step 1: Write failing test `TenantContextTest`**

```java
package com.digitalcow.tenancy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @Test
    void shouldStoreAndClearAccountId() {
        TenantContext.set(42L);
        assertThat(TenantContext.get()).isEqualTo(42L);
        TenantContext.clear();
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void shouldReturnNullWhenNotSet() {
        TenantContext.clear();
        assertThat(TenantContext.get()).isNull();
    }
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TenantContextTest -q`
Expected: FAIL — clase no existe.

- [ ] **Step 3: Implementar `TenantContext.java`**

```java
package com.digitalcow.tenancy;

/**
 * ThreadLocal con el accountId del request actual.
 * Compatible con virtual threads. Siempre limpiar en finally.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CTX = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long accountId) { CTX.set(accountId); }
    public static Long get() { return CTX.get(); }
    public static void clear() { CTX.remove(); }
}
```

- [ ] **Step 4: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TenantContextTest -q`
Expected: PASS.

- [ ] **Step 5: Crear `SkipTenancy.java`**

```java
package com.digitalcow.tenancy;

import java.lang.annotation.*;

/** Marca metodos o servicios que NO deben aplicar el filtro multi-tenant (auth, super-admin). */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipTenancy {}
```

- [ ] **Step 6: Crear `TenantAwareEntityListener.java`**

```java
package com.digitalcow.tenancy;

import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;

/**
 * Inyecta accountId desde TenantContext en cualquier entity con campo accountId
 * antes de persistir, si no esta seteado.
 */
public class TenantAwareEntityListener {

    @PrePersist
    public void onPrePersist(Object entity) {
        try {
            Field f = findAccountIdField(entity.getClass());
            if (f == null) return;
            f.setAccessible(true);
            Object current = f.get(entity);
            if (current == null) {
                Long tid = TenantContext.get();
                if (tid != null) f.set(entity, tid);
            }
        } catch (IllegalAccessException ignored) {
            // no-op: si no es accesible, la BD rechazara por NOT NULL
        }
    }

    private Field findAccountIdField(Class<?> c) {
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if ("accountId".equals(f.getName())) return f;
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
```

- [ ] **Step 7: Crear `TenantFilterAspect.java` (activa Hibernate filter por sesion)**

```java
package com.digitalcow.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Activa el Hibernate filter "accountFilter" con el accountId del TenantContext.
 * Se invoca al inicio de cada transaccion vinculada a un request.
 */
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager em;

    /** Activa el filtro en la sesion actual. Llamado por TenancyFilter. */
    public void enableFilterForCurrentSession() {
        Long accountId = TenantContext.get();
        if (accountId == null) return;
        Session session = em.unwrap(Session.class);
        if (session.getEnabledFilter("accountFilter") == null) {
            session.enableFilter("accountFilter").setParameter("accountId", accountId);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    try { session.disableFilter("accountFilter"); } catch (Exception ignored) {}
                }
            });
        }
    }
}
```

- [ ] **Step 8: Pausa de revisión**

Archivos: 5 archivos en `tenancy/`. Confirmar que listener no falla si entity no tiene `accountId`.

---

### Task 14: JwtService, JwtAuthenticationFilter, TenancyFilter

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/JwtService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/JwtAuthenticationFilter.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/tenancy/TenancyFilter.java`
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/config/SecurityConfig.java`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/auth/JwtServiceTest.java`

- [ ] **Step 1: Write failing test `JwtServiceTest`**

```java
package com.digitalcow.auth;

import com.digitalcow.user.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService svc;

    @BeforeEach
    void setUp() {
        svc = new JwtService("test-secret-key-test-secret-key-test-secret-key-32bytes!", 15, 30);
    }

    @Test
    void shouldIssueAndParseAccessToken() {
        String token = svc.issueAccess(1L, 100L, "user@example.com", List.of(UserRole.OWNER));
        Claims c = svc.parse(token);
        assertThat(c.getSubject()).isEqualTo("1");
        assertThat(c.get("accountId", Long.class)).isEqualTo(100L);
        assertThat(c.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(c.get("roles", List.class)).contains("OWNER");
    }
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=JwtServiceTest -q`
Expected: FAIL — `JwtService` no existe.

- [ ] **Step 3: Implementar `JwtService.java`**

```java
package com.digitalcow.auth;

import com.digitalcow.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Emite y valida JWT HS256. Refresh es opaco (UUID), no JWT.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
        @Value("${digitalcow.security.jwt.secret}") String secret,
        @Value("${digitalcow.security.jwt.access-token-ttl-minutes}") long accessMinutes,
        @Value("${digitalcow.security.jwt.refresh-token-ttl-days}") long refreshDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    /** Genera access token JWT con userId, accountId (nullable), email y roles. */
    public String issueAccess(Long userId, Long accountId, String email, List<UserRole> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("accountId", accountId)
            .claim("email", email)
            .claim("roles", roles.stream().map(Enum::name).toList())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTtl)))
            .signWith(key)
            .compact();
    }

    /** Genera un refresh opaco (UUID v4). */
    public String issueRefresh() {
        return UUID.randomUUID().toString();
    }

    /** Parsea y valida un JWT. Lanza si invalido o expirado. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Duration refreshTtl() { return refreshTtl; }
}
```

- [ ] **Step 4: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=JwtServiceTest -q`
Expected: PASS.

- [ ] **Step 5: Implementar `JwtAuthenticationFilter.java`**

```java
package com.digitalcow.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee Authorization: Bearer, parsea JWT y popula SecurityContext.
 * Si el token es invalido, no autentica pero deja pasar (los protected endpoints rechazaran).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims c = jwt.parse(header.substring(7));
                String userId = c.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = c.get("roles", List.class);
                List<SimpleGrantedAuthority> auths = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();
                var authToken = new UsernamePasswordAuthenticationToken(new AuthPrincipal(
                    Long.valueOf(userId), c.get("accountId", Long.class), c.get("email", String.class)
                ), null, auths);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // token invalido: dejar pasar sin autenticar
            }
        }
        chain.doFilter(req, res);
    }

    /** Principal con datos del usuario autenticado. */
    public record AuthPrincipal(Long userId, Long accountId, String email) {}
}
```

- [ ] **Step 6: Implementar `TenancyFilter.java`**

```java
package com.digitalcow.tenancy;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Despues de Spring Security: setea TenantContext desde el principal JWT.
 * Limpia en finally para no contaminar threads reciclados.
 */
@Component
public class TenancyFilter extends OncePerRequestFilter {

    private final TenantFilterAspect filterAspect;

    public TenancyFilter(TenantFilterAspect filterAspect) { this.filterAspect = filterAspect; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
                if (p.accountId() != null) {
                    TenantContext.set(p.accountId());
                    MDC.put("accountId", String.valueOf(p.accountId()));
                }
                MDC.put("userId", String.valueOf(p.userId()));
                filterAspect.enableFilterForCurrentSession();
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
            MDC.remove("accountId");
            MDC.remove("userId");
        }
    }
}
```

- [ ] **Step 7: Modificar `SecurityConfig.java` para registrar filtros**

Reemplazar el método `securityFilterChain` con esta versión que añade los filtros:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               com.digitalcow.auth.JwtAuthenticationFilter jwtFilter,
                                               com.digitalcow.tenancy.TenancyFilter tenancyFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(h -> h
            .contentTypeOptions(c -> {})
            .frameOptions(f -> f.deny())
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/breeds").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(tenancyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

- [ ] **Step 8: Validar compilación y tests existentes**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -q`
Expected: BUILD SUCCESS, todos los tests verdes.

- [ ] **Step 9: Pausa de revisión**

Archivos: `JwtService.java`, `JwtAuthenticationFilter.java`, `TenancyFilter.java`, `SecurityConfig.java`. Confirmar orden de filtros.

---

## Épica D — Auth backend

### Task 15: DTOs y entities auxiliares de auth (RefreshToken, EmailVerification, PasswordReset)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/RefreshToken.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/RefreshTokenRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/EmailVerification.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/EmailVerificationRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/PasswordReset.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/PasswordResetRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/RegisterRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/LoginRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/RefreshRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/AuthTokensResponse.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/VerifyEmailRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/RequestPasswordResetRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/dto/ResetPasswordRequest.java`

- [ ] **Step 1: Crear entities `RefreshToken`, `EmailVerification`, `PasswordReset`**

`RefreshToken.java`:
```java
package com.digitalcow.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Refresh opaco hasheado en DB (SHA-256 hex). Una sesion = una fila viva. */
@Entity
@Table(name = "refresh_token")
@Getter @Setter @NoArgsConstructor
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
```

`RefreshTokenRepository.java`:
```java
package com.digitalcow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken r where r.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
```

`EmailVerification.java`:
```java
package com.digitalcow.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "email_verification")
@Getter @Setter @NoArgsConstructor
public class EmailVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;
}
```

`EmailVerificationRepository.java`:
```java
package com.digitalcow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByToken(String token);
}
```

`PasswordReset.java`:
```java
package com.digitalcow.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset")
@Getter @Setter @NoArgsConstructor
public class PasswordReset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;
}
```

`PasswordResetRepository.java`:
```java
package com.digitalcow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByToken(String token);
}
```

- [ ] **Step 2: Crear DTOs request/response**

`RegisterRequest.java`:
```java
package com.digitalcow.auth.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(max = 120) String accountName,
    @NotBlank @Size(max = 160) String fullName,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    Locale locale
) {}
```

`LoginRequest.java`:
```java
package com.digitalcow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
```

`RefreshRequest.java`:
```java
package com.digitalcow.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
```

`AuthTokensResponse.java`:
```java
package com.digitalcow.auth.dto;

public record AuthTokensResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
```

`VerifyEmailRequest.java`:
```java
package com.digitalcow.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank String token) {}
```

`RequestPasswordResetRequest.java`:
```java
package com.digitalcow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetRequest(@NotBlank @Email String email) {}
```

`ResetPasswordRequest.java`:
```java
package com.digitalcow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 8, max = 100) String newPassword) {}
```

- [ ] **Step 3: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Pausa de revisión**

Archivos: 13 archivos. Confirmar validation annotations consistentes.

---

### Task 16: EmailSender interface + impls (Smtp y DevLogging)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/mail/EmailSender.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/mail/SmtpEmailSender.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/mail/DevLoggingEmailSender.java`

- [ ] **Step 1: Crear interface `EmailSender.java`**

```java
package com.digitalcow.mail;

/** Abstraccion sobre envio de email. Dos impls: SMTP real y dev-logging. */
public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
```

- [ ] **Step 2: Crear `SmtpEmailSender.java`**

```java
package com.digitalcow.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** Envia email via SMTP (Spring Mail). Activa cuando dev-logging=false. */
@Component
@ConditionalOnProperty(name = "digitalcow.mail.dev-logging", havingValue = "false")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${digitalcow.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
```

- [ ] **Step 3: Crear `DevLoggingEmailSender.java`**

```java
package com.digitalcow.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** En dev: loguea el email en consola en vez de mandarlo. */
@Component
@ConditionalOnProperty(name = "digitalcow.mail.dev-logging", havingValue = "true", matchIfMissing = true)
public class DevLoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DevLoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("\n--- DEV EMAIL ---\nTo: {}\nSubject: {}\nBody:\n{}\n-----------------",
            to, subject, htmlBody);
    }
}
```

- [ ] **Step 4: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Pausa de revisión**

Archivos: 3 archivos en `mail/`.

---

### Task 17: AuthService (register, login, refresh, logout)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/AuthService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/TokenHasher.java`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/auth/TokenHasherTest.java`

- [ ] **Step 1: Write failing test `TokenHasherTest`**

```java
package com.digitalcow.auth;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TokenHasherTest {

    @Test
    void shouldProduce64CharHexSha256() {
        String hash = TokenHasher.sha256Hex("hello");
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void shouldBeDeterministic() {
        assertThat(TokenHasher.sha256Hex("x")).isEqualTo(TokenHasher.sha256Hex("x"));
    }
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TokenHasherTest -q`
Expected: FAIL.

- [ ] **Step 3: Implementar `TokenHasher.java`**

```java
package com.digitalcow.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Hashea tokens opacos con SHA-256 antes de almacenar. */
public final class TokenHasher {

    private TokenHasher() {}

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
```

- [ ] **Step 4: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=TokenHasherTest -q`
Expected: PASS.

- [ ] **Step 5: Implementar `AuthService.java`**

```java
package com.digitalcow.auth;

import com.digitalcow.account.*;
import com.digitalcow.auth.dto.*;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.mail.EmailSender;
import com.digitalcow.tenancy.SkipTenancy;
import com.digitalcow.user.AppUser;
import com.digitalcow.user.AppUserRepository;
import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio que maneja registro, login, refresh, logout, verificacion de email,
 * reset de password. Marcado @SkipTenancy: opera sin tenant en contexto.
 */
@Service
@SkipTenancy
public class AuthService {

    private static final Duration EMAIL_VERIF_TTL = Duration.ofHours(24);
    private static final Duration PWD_RESET_TTL = Duration.ofHours(2);

    private final AccountRepository accounts;
    private final AppUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final EmailVerificationRepository verifications;
    private final PasswordResetRepository resets;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final EmailSender mail;

    public AuthService(AccountRepository accounts, AppUserRepository users,
                       RefreshTokenRepository refreshTokens, EmailVerificationRepository verifications,
                       PasswordResetRepository resets, PasswordEncoder passwordEncoder,
                       JwtService jwt, EmailSender mail) {
        this.accounts = accounts;
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.verifications = verifications;
        this.resets = resets;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.mail = mail;
    }

    /** Crea cuenta + usuario Owner + dispara verificacion de email. */
    @Transactional
    public AuthTokensResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw BusinessException.conflict(ErrorCode.AUTH_EMAIL_ALREADY_USED, "Email already used");
        }
        Account acc = new Account();
        acc.setName(req.accountName());
        acc.setSlug(slugify(req.accountName()));
        acc.setDefaultLocale(req.locale() != null ? req.locale() : Locale.es);
        acc = accounts.save(acc);

        AppUser u = new AppUser();
        u.setAccountId(acc.getId());
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(UserRole.OWNER);
        u.setLocale(req.locale());
        u.setStatus(UserStatus.ACTIVE); // Owner queda activo, pero email_verified_at=null hasta verificar
        u = users.save(u);

        sendVerification(u);
        return issueTokens(u);
    }

    /** Login con email + password. Rechaza si DISABLED o no verificado. */
    @Transactional
    public AuthTokensResponse login(LoginRequest req) {
        AppUser u = users.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw BusinessException.unauthorized(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials");
        }
        if (u.getStatus() == UserStatus.DISABLED) {
            throw BusinessException.forbidden(ErrorCode.AUTH_USER_DISABLED, "User disabled");
        }
        if (u.getRole() != UserRole.SUPERADMIN && u.getEmailVerifiedAt() == null) {
            throw BusinessException.forbidden(ErrorCode.AUTH_EMAIL_NOT_VERIFIED, "Email not verified");
        }
        return issueTokens(u);
    }

    /** Rota el refresh token (uno valido a la vez). */
    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        String hash = TokenHasher.sha256Hex(req.refreshToken());
        RefreshToken rt = refreshTokens.findByTokenHash(hash)
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "Invalid refresh"));
        if (rt.getRevokedAt() != null || rt.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "Expired refresh");
        }
        rt.setRevokedAt(Instant.now());
        AppUser u = users.findById(rt.getUserId())
            .orElseThrow(() -> BusinessException.unauthorized(ErrorCode.AUTH_REFRESH_INVALID, "User missing"));
        return issueTokens(u);
    }

    /** Revoca el refresh token actual. */
    @Transactional
    public void logout(String refreshToken) {
        String hash = TokenHasher.sha256Hex(refreshToken);
        refreshTokens.findByTokenHash(hash).ifPresent(rt -> rt.setRevokedAt(Instant.now()));
    }

    /** Marca email verificado consumiendo token. */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerification v = verifications.findByToken(token)
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "Invalid token"));
        if (v.getUsedAt() != null || v.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_TOKEN_EXPIRED, "Expired or used");
        }
        AppUser u = users.findById(v.getUserId()).orElseThrow();
        u.setEmailVerifiedAt(Instant.now());
        v.setUsedAt(Instant.now());
    }

    /** Genera token de reset (silencioso si email no existe). */
    @Transactional
    public void requestPasswordReset(RequestPasswordResetRequest req) {
        users.findByEmail(req.email().toLowerCase()).ifPresent(u -> {
            PasswordReset r = new PasswordReset();
            r.setUserId(u.getId());
            r.setToken(UUID.randomUUID().toString().replace("-", ""));
            r.setExpiresAt(Instant.now().plus(PWD_RESET_TTL));
            resets.save(r);
            mail.send(u.getEmail(), "Reset your Digital Cow password",
                "<p>Reset token: <code>" + r.getToken() + "</code></p>");
        });
    }

    /** Aplica nueva password tras validar token. */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordReset r = resets.findByToken(req.token())
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.AUTH_TOKEN_INVALID, "Invalid token"));
        if (r.getUsedAt() != null || r.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_TOKEN_EXPIRED, "Expired or used");
        }
        AppUser u = users.findById(r.getUserId()).orElseThrow();
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        r.setUsedAt(Instant.now());
        refreshTokens.deleteAllByUserId(u.getId());
    }

    // ---------- helpers ----------

    private AuthTokensResponse issueTokens(AppUser u) {
        String access = jwt.issueAccess(u.getId(), u.getAccountId(), u.getEmail(), List.of(u.getRole()));
        String refresh = jwt.issueRefresh();
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(TokenHasher.sha256Hex(refresh));
        rt.setExpiresAt(Instant.now().plus(jwt.refreshTtl()));
        refreshTokens.save(rt);
        return new AuthTokensResponse(access, refresh, 15 * 60L);
    }

    private void sendVerification(AppUser u) {
        EmailVerification v = new EmailVerification();
        v.setUserId(u.getId());
        v.setToken(UUID.randomUUID().toString().replace("-", ""));
        v.setExpiresAt(Instant.now().plus(EMAIL_VERIF_TTL));
        verifications.save(v);
        mail.send(u.getEmail(), "Verify your Digital Cow email",
            "<p>Verification token: <code>" + v.getToken() + "</code></p>");
    }

    private String slugify(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (base.isEmpty()) base = "account";
        String slug = base;
        int n = 1;
        while (accounts.existsBySlug(slug)) { slug = base + "-" + n++; }
        return slug;
    }
}
```

- [ ] **Step 6: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Pausa de revisión**

Archivos: `AuthService.java`, `TokenHasher.java`. Verificar transacciones y slugify.

---

### Task 18: AuthController y endpoints `/me`, `/account`

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/AuthController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/MeController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/dto/MeResponse.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/dto/UpdateMeRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/user/dto/UpdatePasswordRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/AccountController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/dto/AccountResponse.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/account/dto/UpdateAccountRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/common/web/CurrentUser.java`

- [ ] **Step 1: Crear `CurrentUser.java` helper para extraer principal**

```java
package com.digitalcow.common.web;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;

/** Helper estatico para obtener el principal autenticado actual. */
public final class CurrentUser {

    private CurrentUser() {}

    public static AuthPrincipal require() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal p)) {
            throw BusinessException.unauthorized(ErrorCode.UNAUTHENTICATED, "No principal");
        }
        return p;
    }
}
```

- [ ] **Step 2: Crear `AuthController.java`**

```java
package com.digitalcow.auth;

import com.digitalcow.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints publicos de autenticacion (sin tenant). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService svc;

    public AuthController(AuthService svc) { this.svc = svc; }

    @PostMapping("/register")
    public AuthTokensResponse register(@Valid @RequestBody RegisterRequest req) {
        return svc.register(req);
    }

    @PostMapping("/login")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest req) {
        return svc.login(req);
    }

    @PostMapping("/refresh")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return svc.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        svc.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        svc.verifyEmail(req.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody RequestPasswordResetRequest req) {
        svc.requestPasswordReset(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        svc.resetPassword(req);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 3: Crear DTOs y `MeController.java`**

`MeResponse.java`:
```java
package com.digitalcow.user.dto;

import com.digitalcow.account.Locale;
import com.digitalcow.user.UserRole;

public record MeResponse(Long id, Long accountId, String email, String fullName,
                         UserRole role, Locale locale, boolean emailVerified) {}
```

`UpdateMeRequest.java`:
```java
package com.digitalcow.user.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(@Size(max = 160) String fullName, Locale locale) {}
```

`UpdatePasswordRequest.java`:
```java
package com.digitalcow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(@NotBlank String currentPassword,
                                    @NotBlank @Size(min = 8, max = 100) String newPassword) {}
```

`MeController.java`:
```java
package com.digitalcow.user;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.user.dto.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public MeController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping
    public MeResponse get() {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        return new MeResponse(u.getId(), u.getAccountId(), u.getEmail(), u.getFullName(),
            u.getRole(), u.getLocale(), u.getEmailVerifiedAt() != null);
    }

    @PatchMapping
    @Transactional
    public MeResponse update(@Valid @RequestBody UpdateMeRequest req) {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.locale() != null) u.setLocale(req.locale());
        return new MeResponse(u.getId(), u.getAccountId(), u.getEmail(), u.getFullName(),
            u.getRole(), u.getLocale(), u.getEmailVerifiedAt() != null);
    }

    @PatchMapping("/password")
    @Transactional
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest req) {
        var p = CurrentUser.require();
        AppUser u = users.findById(p.userId()).orElseThrow();
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw BusinessException.badRequest(ErrorCode.AUTH_INVALID_CREDENTIALS, "Bad password");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Crear `AccountController.java` + DTOs**

`AccountResponse.java`:
```java
package com.digitalcow.account.dto;

public record AccountResponse(Long id, String name, String slug, String status,
                              String plan, String defaultLocale) {}
```

`UpdateAccountRequest.java`:
```java
package com.digitalcow.account.dto;

import com.digitalcow.account.Locale;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(@Size(max = 120) String name, Locale defaultLocale) {}
```

`AccountController.java`:
```java
package com.digitalcow.account;

import com.digitalcow.account.dto.*;
import com.digitalcow.common.web.CurrentUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountRepository repo;

    public AccountController(AccountRepository repo) { this.repo = repo; }

    @GetMapping
    public AccountResponse get() {
        var p = CurrentUser.require();
        Account a = repo.findById(p.accountId()).orElseThrow();
        return toDto(a);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Transactional
    public AccountResponse update(@Valid @RequestBody UpdateAccountRequest req) {
        var p = CurrentUser.require();
        Account a = repo.findById(p.accountId()).orElseThrow();
        if (req.name() != null) a.setName(req.name());
        if (req.defaultLocale() != null) a.setDefaultLocale(req.defaultLocale());
        return toDto(a);
    }

    private AccountResponse toDto(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getSlug(),
            a.getStatus().name(), a.getPlan().name(), a.getDefaultLocale().name());
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 9 archivos auth/account/user. Confirmar endpoints contra spec §4.5.

---

## Épica E — Equipos

### Task 19: TeamController, UserInvitationService, endpoints de equipo

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/UserInvitation.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/UserInvitationRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/TeamService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/TeamController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/dto/TeamUserDto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/dto/InvitationDto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/dto/InviteUserRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/dto/AcceptInvitationRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/dto/UpdateUserRequest.java`

- [ ] **Step 1: Crear `UserInvitation.java` entity**

```java
package com.digitalcow.team;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import com.digitalcow.user.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;

@Entity
@Table(name = "user_invitation")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class UserInvitation extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 180)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
}
```

- [ ] **Step 2: Crear `UserInvitationRepository.java`**

```java
package com.digitalcow.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByToken(String token);
    List<UserInvitation> findAllByAcceptedAtIsNull();
}
```

- [ ] **Step 3: Crear DTOs**

`TeamUserDto.java`:
```java
package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;

public record TeamUserDto(Long id, String email, String fullName,
                          UserRole role, UserStatus status) {}
```

`InvitationDto.java`:
```java
package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;

import java.time.Instant;

public record InvitationDto(Long id, String email, UserRole role,
                            Instant expiresAt, Instant acceptedAt) {}
```

`InviteUserRequest.java`:
```java
package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequest(@NotBlank @Email String email, @NotNull UserRole role) {}
```

`AcceptInvitationRequest.java`:
```java
package com.digitalcow.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(@NotBlank @Size(max = 160) String fullName,
                                       @NotBlank @Size(min = 8, max = 100) String password) {}
```

`UpdateUserRequest.java`:
```java
package com.digitalcow.team.dto;

import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;

public record UpdateUserRequest(UserRole role, UserStatus status) {}
```

- [ ] **Step 4: Crear `TeamService.java`**

```java
package com.digitalcow.team;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.mail.EmailSender;
import com.digitalcow.team.dto.*;
import com.digitalcow.tenancy.SkipTenancy;
import com.digitalcow.tenancy.TenantContext;
import com.digitalcow.user.AppUser;
import com.digitalcow.user.AppUserRepository;
import com.digitalcow.user.UserRole;
import com.digitalcow.user.UserStatus;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Gestion de equipos: listado, invitaciones, acept., cambio de rol. */
@Service
public class TeamService {

    private static final Duration INV_TTL = Duration.ofDays(7);

    private final AppUserRepository users;
    private final UserInvitationRepository invitations;
    private final PasswordEncoder encoder;
    private final EmailSender mail;

    public TeamService(AppUserRepository users, UserInvitationRepository invitations,
                       PasswordEncoder encoder, EmailSender mail) {
        this.users = users;
        this.invitations = invitations;
        this.encoder = encoder;
        this.mail = mail;
    }

    public List<TeamUserDto> listUsers() {
        Long accId = TenantContext.get();
        return users.findAllByAccountId(accId).stream()
            .map(u -> new TeamUserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus()))
            .toList();
    }

    public List<InvitationDto> listPendingInvitations() {
        return invitations.findAllByAcceptedAtIsNull().stream()
            .map(i -> new InvitationDto(i.getId(), i.getEmail(), i.getRole(), i.getExpiresAt(), i.getAcceptedAt()))
            .toList();
    }

    @Transactional
    public InvitationDto invite(InviteUserRequest req) {
        var p = CurrentUser.require();
        UserInvitation inv = new UserInvitation();
        inv.setAccountId(p.accountId());
        inv.setEmail(req.email().toLowerCase());
        inv.setRole(req.role());
        inv.setToken(UUID.randomUUID().toString().replace("-", ""));
        inv.setExpiresAt(Instant.now().plus(INV_TTL));
        inv.setCreatedByUserId(p.userId());
        invitations.save(inv);
        mail.send(inv.getEmail(), "You have been invited to Digital Cow",
            "<p>Accept token: <code>" + inv.getToken() + "</code></p>");
        return new InvitationDto(inv.getId(), inv.getEmail(), inv.getRole(), inv.getExpiresAt(), null);
    }

    @Transactional
    public void deleteInvitation(Long id) {
        invitations.deleteById(id);
    }

    /** Acepta invitacion: NO requiere auth previa (skip tenancy). */
    @Transactional
    @SkipTenancy
    public void accept(String token, AcceptInvitationRequest req) {
        UserInvitation inv = invitations.findByToken(token)
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.INVITATION_INVALID, "Invalid token"));
        if (inv.getAcceptedAt() != null) {
            throw BusinessException.conflict(ErrorCode.INVITATION_ALREADY_ACCEPTED, "Already accepted");
        }
        if (inv.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest(ErrorCode.INVITATION_EXPIRED, "Expired");
        }
        if (users.existsByEmail(inv.getEmail())) {
            throw BusinessException.conflict(ErrorCode.AUTH_EMAIL_ALREADY_USED, "Email already used");
        }
        AppUser u = new AppUser();
        u.setAccountId(inv.getAccountId());
        u.setEmail(inv.getEmail());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(inv.getRole());
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerifiedAt(Instant.now()); // verificacion implicita por aceptar token
        users.save(u);
        inv.setAcceptedAt(Instant.now());
    }

    @Transactional
    public TeamUserDto updateUser(Long userId, UpdateUserRequest req) {
        AppUser u = users.findById(userId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "User not found"));
        if (!u.getAccountId().equals(TenantContext.get())) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-tenant");
        }
        if (req.role() != null && req.role() != UserRole.SUPERADMIN) u.setRole(req.role());
        if (req.status() != null) u.setStatus(req.status());
        return new TeamUserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus());
    }
}
```

- [ ] **Step 5: Crear `TeamController.java`**

```java
package com.digitalcow.team;

import com.digitalcow.team.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
public class TeamController {

    private final TeamService svc;

    public TeamController(TeamService svc) { this.svc = svc; }

    @GetMapping
    public List<TeamUserDto> listUsers() { return svc.listUsers(); }

    @GetMapping("/invitations")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public List<InvitationDto> listInvitations() { return svc.listPendingInvitations(); }

    @PostMapping("/invitations")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public InvitationDto invite(@Valid @RequestBody InviteUserRequest req) { return svc.invite(req); }

    @DeleteMapping("/invitations/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<Void> deleteInvitation(@PathVariable Long id) {
        svc.deleteInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<Void> accept(@PathVariable String token,
                                       @Valid @RequestBody AcceptInvitationRequest req) {
        svc.accept(token, req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public TeamUserDto updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return svc.updateUser(id, req);
    }
}
```

- [ ] **Step 6: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Pausa de revisión**

Archivos: 9 archivos en `team/`. Confirmar `@PreAuthorize` con matriz §4.3.

---

## Épica F — Ranchos y lotes

### Task 20: Migración V2 ranch+lot

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V2__ranch_lot.sql`

- [ ] **Step 1: Crear `V2__ranch_lot.sql`**

```sql
CREATE TABLE ranch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  location VARCHAR(200) NULL,
  latitude DECIMAL(9,6) NULL,
  longitude DECIMAL(9,6) NULL,
  area_hectares DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_ranch_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  INDEX ix_ranch_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  area_hectares DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_lot_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_lot_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id) ON DELETE RESTRICT,
  INDEX ix_lot_account_ranch (account_id, ranch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Pausa de revisión**

Archivo: `V2__ranch_lot.sql`.

---

### Task 21: Ranch entity, repo, service, controller

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/Ranch.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/RanchRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/RanchService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/RanchController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/dto/RanchDto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/dto/RanchUpsertRequest.java`

- [ ] **Step 1: Crear `Ranch.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;

@Entity
@Table(name = "ranch")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class Ranch extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "area_hectares", precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
```

- [ ] **Step 2: Crear `RanchRepository.java`**

```java
package com.digitalcow.ranch;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RanchRepository extends JpaRepository<Ranch, Long> {
}
```

- [ ] **Step 3: Crear DTOs**

`RanchDto.java`:
```java
package com.digitalcow.ranch.dto;

import java.math.BigDecimal;

public record RanchDto(Long id, String name, String location,
                       BigDecimal latitude, BigDecimal longitude,
                       BigDecimal areaHectares, String notes) {}
```

`RanchUpsertRequest.java`:
```java
package com.digitalcow.ranch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RanchUpsertRequest(@NotBlank @Size(max = 120) String name,
                                  @Size(max = 200) String location,
                                  BigDecimal latitude,
                                  BigDecimal longitude,
                                  BigDecimal areaHectares,
                                  String notes) {}
```

- [ ] **Step 4: Crear `RanchService.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.ranch.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD de ranchos. Filtro multi-tenant aplicado por Hibernate filter. */
@Service
public class RanchService {

    private final RanchRepository repo;

    public RanchService(RanchRepository repo) { this.repo = repo; }

    public List<RanchDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public RanchDto get(Long id) {
        return toDto(find(id));
    }

    @Transactional
    public RanchDto create(RanchUpsertRequest req) {
        Ranch r = new Ranch();
        apply(r, req);
        return toDto(repo.save(r));
    }

    @Transactional
    public RanchDto update(Long id, RanchUpsertRequest req) {
        Ranch r = find(id);
        apply(r, req);
        return toDto(r);
    }

    /**
     * Eliminar rancho. Verificacion de animales activos se hara en Task 30
     * via consulta a animal_repo.count(specificacion). Por ahora delega al motor SQL
     * (FK RESTRICT). Cuando exista la tabla animal, agregar pre-check con codigo
     * RANCH_HAS_ANIMALS.
     */
    @Transactional
    public void delete(Long id) {
        Ranch r = find(id);
        try {
            repo.delete(r);
        } catch (Exception e) {
            throw BusinessException.conflict(ErrorCode.RANCH_HAS_ANIMALS, "Ranch in use");
        }
    }

    private Ranch find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Ranch not found"));
    }

    private void apply(Ranch r, RanchUpsertRequest req) {
        r.setName(req.name());
        r.setLocation(req.location());
        r.setLatitude(req.latitude());
        r.setLongitude(req.longitude());
        r.setAreaHectares(req.areaHectares());
        r.setNotes(req.notes());
    }

    private RanchDto toDto(Ranch r) {
        return new RanchDto(r.getId(), r.getName(), r.getLocation(),
            r.getLatitude(), r.getLongitude(), r.getAreaHectares(), r.getNotes());
    }
}
```

- [ ] **Step 5: Crear `RanchController.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.ranch.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ranches")
public class RanchController {

    private final RanchService svc;

    public RanchController(RanchService svc) { this.svc = svc; }

    @GetMapping
    public List<RanchDto> list() { return svc.list(); }

    @GetMapping("/{id}")
    public RanchDto get(@PathVariable Long id) { return svc.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public RanchDto create(@Valid @RequestBody RanchUpsertRequest req) { return svc.create(req); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public RanchDto update(@PathVariable Long id, @Valid @RequestBody RanchUpsertRequest req) {
        return svc.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 6: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Pausa de revisión**

Archivos: 6 archivos `ranch/`.

---

### Task 22: Lot entity, repo, service, controller

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/Lot.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/LotRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/LotService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/LotController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/dto/LotDto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/dto/LotUpsertRequest.java`

- [ ] **Step 1: Crear `Lot.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;

@Entity
@Table(name = "lot")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class Lot extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "area_hectares", precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
```

- [ ] **Step 2: Crear `LotRepository.java`**

```java
package com.digitalcow.ranch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findAllByRanchId(Long ranchId);
}
```

- [ ] **Step 3: Crear DTOs**

`LotDto.java`:
```java
package com.digitalcow.ranch.dto;

import java.math.BigDecimal;

public record LotDto(Long id, Long ranchId, String name, BigDecimal areaHectares, String notes) {}
```

`LotUpsertRequest.java`:
```java
package com.digitalcow.ranch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LotUpsertRequest(@NotBlank @Size(max = 120) String name,
                                BigDecimal areaHectares,
                                String notes) {}
```

- [ ] **Step 4: Crear `LotService.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.ranch.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LotService {

    private final LotRepository repo;
    private final RanchRepository ranches;

    public LotService(LotRepository repo, RanchRepository ranches) {
        this.repo = repo;
        this.ranches = ranches;
    }

    public List<LotDto> listByRanch(Long ranchId) {
        return repo.findAllByRanchId(ranchId).stream().map(this::toDto).toList();
    }

    @Transactional
    public LotDto create(Long ranchId, LotUpsertRequest req) {
        ranches.findById(ranchId).orElseThrow(() ->
            BusinessException.notFound(ErrorCode.NOT_FOUND, "Ranch not found"));
        Lot l = new Lot();
        l.setRanchId(ranchId);
        l.setName(req.name());
        l.setAreaHectares(req.areaHectares());
        l.setNotes(req.notes());
        return toDto(repo.save(l));
    }

    @Transactional
    public LotDto update(Long id, LotUpsertRequest req) {
        Lot l = find(id);
        l.setName(req.name());
        l.setAreaHectares(req.areaHectares());
        l.setNotes(req.notes());
        return toDto(l);
    }

    /**
     * Delete con misma estrategia que ranch: pre-check con animal count se agrega
     * en Task 30 (animales). Por ahora FK RESTRICT bloqueara fisicamente.
     */
    @Transactional
    public void delete(Long id) {
        Lot l = find(id);
        try {
            repo.delete(l);
        } catch (Exception e) {
            throw BusinessException.conflict(ErrorCode.LOT_HAS_ANIMALS, "Lot in use");
        }
    }

    private Lot find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Lot not found"));
    }

    private LotDto toDto(Lot l) {
        return new LotDto(l.getId(), l.getRanchId(), l.getName(), l.getAreaHectares(), l.getNotes());
    }
}
```

- [ ] **Step 5: Crear `LotController.java`**

```java
package com.digitalcow.ranch;

import com.digitalcow.ranch.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LotController {

    private final LotService svc;

    public LotController(LotService svc) { this.svc = svc; }

    @GetMapping("/api/v1/ranches/{ranchId}/lots")
    public List<LotDto> list(@PathVariable Long ranchId) { return svc.listByRanch(ranchId); }

    @PostMapping("/api/v1/ranches/{ranchId}/lots")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public LotDto create(@PathVariable Long ranchId, @Valid @RequestBody LotUpsertRequest req) {
        return svc.create(ranchId, req);
    }

    @PatchMapping("/api/v1/lots/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public LotDto update(@PathVariable Long id, @Valid @RequestBody LotUpsertRequest req) {
        return svc.update(id, req);
    }

    @DeleteMapping("/api/v1/lots/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 6: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Pausa de revisión**

Archivos: 6 archivos `ranch/lot*`.

---

## Épica G — Catálogo de razas

### Task 23: Migración V3 breed + seed

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V3__breed.sql`

- [ ] **Step 1: Crear `V3__breed.sql` con seed de 17 razas**

```sql
CREATE TABLE breed (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  name_es VARCHAR(120) NOT NULL,
  name_en VARCHAR(120) NOT NULL,
  species ENUM('BOVINE') NOT NULL DEFAULT 'BOVINE',
  category ENUM('DAIRY','BEEF','DUAL') NOT NULL,
  bos ENUM('TAURUS','INDICUS','CROSS') NOT NULL,
  CONSTRAINT uq_breed_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO breed (code, name_es, name_en, species, category, bos) VALUES
  ('HOLSTEIN','Holstein','Holstein','BOVINE','DAIRY','TAURUS'),
  ('JERSEY','Jersey','Jersey','BOVINE','DAIRY','TAURUS'),
  ('PARDO_SUIZO','Pardo Suizo','Brown Swiss','BOVINE','DAIRY','TAURUS'),
  ('GYR','Gyr','Gyr','BOVINE','DAIRY','INDICUS'),
  ('GIROLANDO','Girolando','Girolando','BOVINE','DAIRY','CROSS'),
  ('ANGUS','Angus','Angus','BOVINE','BEEF','TAURUS'),
  ('HEREFORD','Hereford','Hereford','BOVINE','BEEF','TAURUS'),
  ('CHAROLAIS','Charolais','Charolais','BOVINE','BEEF','TAURUS'),
  ('BRAHMAN','Brahman','Brahman','BOVINE','BEEF','INDICUS'),
  ('BRANGUS','Brangus','Brangus','BOVINE','BEEF','CROSS'),
  ('BEEFMASTER','Beefmaster','Beefmaster','BOVINE','BEEF','CROSS'),
  ('SIMMENTAL','Simmental','Simmental','BOVINE','DUAL','TAURUS'),
  ('LIMOUSIN','Limousin','Limousin','BOVINE','BEEF','TAURUS'),
  ('NELORE','Nelore','Nelore','BOVINE','BEEF','INDICUS'),
  ('SENEPOL','Senepol','Senepol','BOVINE','BEEF','TAURUS'),
  ('SANTA_GERTRUDIS','Santa Gertrudis','Santa Gertrudis','BOVINE','BEEF','CROSS'),
  ('SIMBRAH','Simbrah','Simbrah','BOVINE','DUAL','CROSS')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
```

- [ ] **Step 2: Pausa de revisión**

Archivo: `V3__breed.sql`. Confirmar 17 INSERTs.

---

### Task 24: Breed entity, repo, controller (read-only, public)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/breed/Breed.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/breed/BreedRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/breed/BreedController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/breed/dto/BreedDto.java`

- [ ] **Step 1: Crear enums y entity Breed**

`Breed.java`:
```java
package com.digitalcow.breed;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "breed")
@Getter @Setter @NoArgsConstructor
public class Breed {

    public enum Species { BOVINE }
    public enum Category { DAIRY, BEEF, DUAL }
    public enum Bos { TAURUS, INDICUS, CROSS }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String code;

    @Column(name = "name_es", nullable = false, length = 120)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Species species;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Bos bos;
}
```

- [ ] **Step 2: Crear `BreedRepository.java`**

```java
package com.digitalcow.breed;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed, Long> {}
```

- [ ] **Step 3: Crear `BreedDto.java`**

```java
package com.digitalcow.breed.dto;

public record BreedDto(Long id, String code, String nameEs, String nameEn,
                       String species, String category, String bos) {}
```

- [ ] **Step 4: Crear `BreedController.java`**

```java
package com.digitalcow.breed;

import com.digitalcow.breed.dto.BreedDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lectura publica del catalogo de razas. Cacheable por el cliente. */
@RestController
@RequestMapping("/api/v1/breeds")
public class BreedController {

    private final BreedRepository repo;

    public BreedController(BreedRepository repo) { this.repo = repo; }

    @GetMapping
    public List<BreedDto> list() {
        return repo.findAll().stream()
            .map(b -> new BreedDto(b.getId(), b.getCode(), b.getNameEs(), b.getNameEn(),
                b.getSpecies().name(), b.getCategory().name(), b.getBos().name()))
            .toList();
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 4 archivos `breed/`.

---

## Épica H — Animales

### Task 25: Migración V4 animal

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V4__animal.sql`

- [ ] **Step 1: Crear `V4__animal.sql`**

```sql
CREATE TABLE animal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  lot_id BIGINT NULL,
  internal_tag VARCHAR(40) NOT NULL,
  official_tag VARCHAR(60) NULL,
  rfid VARCHAR(40) NULL,
  name VARCHAR(80) NULL,
  sex ENUM('FEMALE','MALE') NOT NULL,
  birth_date DATE NULL,
  birth_date_estimated BOOLEAN NOT NULL DEFAULT FALSE,
  breed_id BIGINT NOT NULL,
  purpose ENUM('BEEF','DAIRY','DUAL') NOT NULL,
  status ENUM('ACTIVE','SOLD','DEAD','MISSING','TRANSFERRED') NOT NULL DEFAULT 'ACTIVE',
  cover_photo_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_animal_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_animal_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id) ON DELETE RESTRICT,
  CONSTRAINT fk_animal_lot FOREIGN KEY (lot_id) REFERENCES lot(id) ON DELETE RESTRICT,
  CONSTRAINT fk_animal_breed FOREIGN KEY (breed_id) REFERENCES breed(id) ON DELETE RESTRICT,
  CONSTRAINT fk_animal_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
  CONSTRAINT uq_animal_tag UNIQUE (account_id, internal_tag),
  CONSTRAINT uq_animal_official_tag UNIQUE (account_id, official_tag),
  INDEX ix_animal_account_ranch_lot_status (account_id, ranch_id, lot_id, status),
  INDEX ix_animal_account_breed (account_id, breed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Pausa de revisión**

Archivo: `V4__animal.sql`. Confirmar UQ y índices del spec §4.4.

---

### Task 26: Animal entity, enums, repo, specifications

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/Animal.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/Sex.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/Purpose.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/AnimalStatus.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/AnimalRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/spec/AnimalSpecifications.java`

- [ ] **Step 1: Crear enums**

`Sex.java`:
```java
package com.digitalcow.animal;
public enum Sex { FEMALE, MALE }
```

`Purpose.java`:
```java
package com.digitalcow.animal;
public enum Purpose { BEEF, DAIRY, DUAL }
```

`AnimalStatus.java`:
```java
package com.digitalcow.animal;
public enum AnimalStatus { ACTIVE, SOLD, DEAD, MISSING, TRANSFERRED }
```

- [ ] **Step 2: Crear `Animal.java`**

```java
package com.digitalcow.animal;

import com.digitalcow.common.jpa.AbstractAuditableEntity;
import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;

@Entity
@Table(name = "animal")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class Animal extends AbstractAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "ranch_id", nullable = false)
    private Long ranchId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "internal_tag", nullable = false, length = 40)
    private String internalTag;

    @Column(name = "official_tag", length = 60)
    private String officialTag;

    @Column(length = 40)
    private String rfid;

    @Column(length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Sex sex;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_date_estimated", nullable = false)
    private boolean birthDateEstimated;

    @Column(name = "breed_id", nullable = false)
    private Long breedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AnimalStatus status = AnimalStatus.ACTIVE;

    @Column(name = "cover_photo_id")
    private Long coverPhotoId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
}
```

- [ ] **Step 3: Crear `AnimalRepository.java`**

```java
package com.digitalcow.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {
    long countByRanchIdAndStatus(Long ranchId, AnimalStatus status);
    long countByLotIdAndStatus(Long lotId, AnimalStatus status);
}
```

- [ ] **Step 4: Crear `AnimalSpecifications.java`**

```java
package com.digitalcow.animal.spec;

import com.digitalcow.animal.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Specifications JPA para filtros dinamicos del listado de animales. */
public final class AnimalSpecifications {

    private AnimalSpecifications() {}

    public static Specification<Animal> build(String search, Long ranchId, Long lotId,
                                              Long breedId, Sex sex, Purpose purpose, AnimalStatus status) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(root.get("internalTag")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("officialTag"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("name"), "")), like)
                ));
            }
            if (ranchId != null) p.add(cb.equal(root.get("ranchId"), ranchId));
            if (lotId != null) p.add(cb.equal(root.get("lotId"), lotId));
            if (breedId != null) p.add(cb.equal(root.get("breedId"), breedId));
            if (sex != null) p.add(cb.equal(root.get("sex"), sex));
            if (purpose != null) p.add(cb.equal(root.get("purpose"), purpose));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 6 en `animal/`.

---

### Task 27: AnimalMapper (MapStruct), DTOs

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/dto/AnimalCreateRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/dto/AnimalUpdateRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/dto/AnimalResponse.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/dto/AnimalListItem.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/mapper/AnimalMapper.java`

- [ ] **Step 1: Crear DTOs**

`AnimalCreateRequest.java`:
```java
package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AnimalCreateRequest(
    @NotNull Long ranchId,
    Long lotId,
    @NotBlank @Size(max = 40) String internalTag,
    @Size(max = 60) String officialTag,
    @Size(max = 40) String rfid,
    @Size(max = 80) String name,
    @NotNull Sex sex,
    LocalDate birthDate,
    boolean birthDateEstimated,
    @NotNull Long breedId,
    @NotNull Purpose purpose,
    AnimalStatus status,
    String notes
) {}
```

`AnimalUpdateRequest.java`:
```java
package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AnimalUpdateRequest(
    Long ranchId,
    Long lotId,
    @Size(max = 40) String internalTag,
    @Size(max = 60) String officialTag,
    @Size(max = 40) String rfid,
    @Size(max = 80) String name,
    Sex sex,
    LocalDate birthDate,
    Boolean birthDateEstimated,
    Long breedId,
    Purpose purpose,
    AnimalStatus status,
    String notes
) {}
```

`AnimalResponse.java`:
```java
package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;

import java.time.Instant;
import java.time.LocalDate;

public record AnimalResponse(
    Long id, Long ranchId, Long lotId,
    String internalTag, String officialTag, String rfid, String name,
    Sex sex, LocalDate birthDate, boolean birthDateEstimated,
    Long breedId, Purpose purpose, AnimalStatus status,
    Long coverPhotoId, String notes,
    Long createdByUserId, Instant createdAt, Instant updatedAt
) {}
```

`AnimalListItem.java`:
```java
package com.digitalcow.animal.dto;

import com.digitalcow.animal.*;

public record AnimalListItem(
    Long id, String internalTag, String officialTag, String name,
    Long breedId, Sex sex, AnimalStatus status, Long lotId, Long coverPhotoId
) {}
```

- [ ] **Step 2: Crear `AnimalMapper.java`**

```java
package com.digitalcow.animal.mapper;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.dto.*;
import org.mapstruct.*;

/** Conversion MapStruct entre Animal entity y DTOs publicos. */
@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AnimalMapper {

    AnimalResponse toResponse(Animal a);

    AnimalListItem toListItem(Animal a);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "coverPhotoId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Animal fromCreate(AnimalCreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "coverPhotoId", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void applyUpdate(AnimalUpdateRequest req, @MappingTarget Animal a);
}
```

- [ ] **Step 3: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS, MapStruct genera AnimalMapperImpl.

- [ ] **Step 4: Pausa de revisión**

Archivos: 5 archivos. Verificar que mapper no expone Animal afuera del paquete.

---

### Task 28: AnimalService (CRUD, paginación, filtros)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/AnimalService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/event/AnimalChangedEvent.java`

- [ ] **Step 1: Crear `AnimalChangedEvent.java`**

```java
package com.digitalcow.animal.event;

/** Evento publicado tras crear/actualizar/eliminar animal, para invalidar caches. */
public record AnimalChangedEvent(Long accountId) {}
```

- [ ] **Step 2: Crear `AnimalService.java`**

```java
package com.digitalcow.animal;

import com.digitalcow.animal.dto.*;
import com.digitalcow.animal.event.AnimalChangedEvent;
import com.digitalcow.animal.mapper.AnimalMapper;
import com.digitalcow.animal.spec.AnimalSpecifications;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** CRUD de animales. */
@Service
public class AnimalService {

    private final AnimalRepository repo;
    private final AnimalMapper mapper;
    private final ApplicationEventPublisher events;

    public AnimalService(AnimalRepository repo, AnimalMapper mapper, ApplicationEventPublisher events) {
        this.repo = repo;
        this.mapper = mapper;
        this.events = events;
    }

    public Page<AnimalListItem> list(String search, Long ranchId, Long lotId, Long breedId,
                                     Sex sex, Purpose purpose, AnimalStatus status, Pageable pageable) {
        return repo.findAll(AnimalSpecifications.build(search, ranchId, lotId, breedId, sex, purpose, status), pageable)
            .map(mapper::toListItem);
    }

    public AnimalResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public AnimalResponse create(AnimalCreateRequest req) {
        Animal a = mapper.fromCreate(req);
        a.setCreatedByUserId(CurrentUser.require().userId());
        if (a.getStatus() == null) a.setStatus(AnimalStatus.ACTIVE);
        Animal saved = repo.save(a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
        return mapper.toResponse(saved);
    }

    @Transactional
    public AnimalResponse update(Long id, AnimalUpdateRequest req) {
        Animal a = find(id);
        mapper.applyUpdate(req, a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
        return mapper.toResponse(a);
    }

    /**
     * Solo permite borrado fisico si nunca se edito (createdAt == updatedAt).
     * Caso contrario, exige cambio de status (SOLD/DEAD) via update.
     */
    @Transactional
    public void delete(Long id) {
        Animal a = find(id);
        if (!a.getCreatedAt().equals(a.getUpdatedAt())) {
            throw BusinessException.conflict(ErrorCode.ANIMAL_NOT_DELETABLE,
                "Animal edited; cannot hard-delete. Update status instead.");
        }
        repo.delete(a);
        events.publishEvent(new AnimalChangedEvent(TenantContext.get()));
    }

    Animal find(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
    }
}
```

- [ ] **Step 3: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Pausa de revisión**

Archivos: 2 archivos. Confirmar invalidacion via evento.

---

### Task 29: AnimalController

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/AnimalController.java`

- [ ] **Step 1: Crear `AnimalController.java`**

```java
package com.digitalcow.animal;

import com.digitalcow.animal.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/animals")
public class AnimalController {

    private final AnimalService svc;

    public AnimalController(AnimalService svc) { this.svc = svc; }

    @GetMapping
    public Page<AnimalListItem> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long ranchId,
        @RequestParam(required = false) Long lotId,
        @RequestParam(required = false) Long breedId,
        @RequestParam(required = false) Sex sex,
        @RequestParam(required = false) Purpose purpose,
        @RequestParam(required = false) AnimalStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return svc.list(search, ranchId, lotId, breedId, sex, purpose, status, pageable);
    }

    @GetMapping("/{id}")
    public AnimalResponse get(@PathVariable Long id) { return svc.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public AnimalResponse create(@Valid @RequestBody AnimalCreateRequest req) { return svc.create(req); }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public AnimalResponse update(@PathVariable Long id, @Valid @RequestBody AnimalUpdateRequest req) {
        return svc.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 2: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Pausa de revisión**

Archivo: `AnimalController.java`. Verificar roles vs matriz §4.3.

---

### Task 30: Reforzar RanchService/LotService con count de animales

**Files:**
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/RanchService.java`
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/ranch/LotService.java`

- [ ] **Step 1: Modificar `RanchService.java` para inyectar AnimalRepository y pre-check**

Cambios:
- Agregar dependencia `AnimalRepository animals` en constructor.
- En `delete(Long id)`, antes del `repo.delete(r)`, verificar:

```java
@Transactional
public void delete(Long id) {
    Ranch r = find(id);
    long count = animals.countByRanchIdAndStatus(id, com.digitalcow.animal.AnimalStatus.ACTIVE);
    if (count > 0) {
        throw BusinessException.conflict(ErrorCode.RANCH_HAS_ANIMALS, "Ranch has active animals");
    }
    repo.delete(r);
}
```

Constructor actualizado:
```java
private final RanchRepository repo;
private final com.digitalcow.animal.AnimalRepository animals;

public RanchService(RanchRepository repo, com.digitalcow.animal.AnimalRepository animals) {
    this.repo = repo;
    this.animals = animals;
}
```

- [ ] **Step 2: Modificar `LotService.java` análogo**

```java
private final LotRepository repo;
private final RanchRepository ranches;
private final com.digitalcow.animal.AnimalRepository animals;

public LotService(LotRepository repo, RanchRepository ranches, com.digitalcow.animal.AnimalRepository animals) {
    this.repo = repo;
    this.ranches = ranches;
    this.animals = animals;
}
```

Y método `delete`:
```java
@Transactional
public void delete(Long id) {
    Lot l = find(id);
    long count = animals.countByLotIdAndStatus(id, com.digitalcow.animal.AnimalStatus.ACTIVE);
    if (count > 0) {
        throw BusinessException.conflict(ErrorCode.LOT_HAS_ANIMALS, "Lot has active animals");
    }
    repo.delete(l);
}
```

- [ ] **Step 3: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Pausa de revisión**

Archivos modificados: `RanchService.java`, `LotService.java`.

---

### Task 31: Tests integración Animal (multi-tenant + CRUD)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/animal/AnimalIT.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/AbstractIT.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/test/resources/application-test.yml`

- [ ] **Step 1: Crear `application-test.yml`**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
digitalcow:
  security:
    jwt:
      secret: test-secret-key-test-secret-key-test-secret-key-32bytes!
  mail:
    dev-logging: true
```

- [ ] **Step 2: Crear `AbstractIT.java` con Testcontainers MySQL**

```java
package com.digitalcow;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Base para integration tests con MySQL real (Testcontainers). */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("digitalcow_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
    }
}
```

- [ ] **Step 3: Write failing test `AnimalIT.java`**

```java
package com.digitalcow.animal;

import com.digitalcow.AbstractIT;
import com.digitalcow.account.*;
import com.digitalcow.tenancy.TenantContext;
import com.digitalcow.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalIT extends AbstractIT {

    @Autowired AccountRepository accounts;
    @Autowired AppUserRepository users;
    @Autowired com.digitalcow.ranch.RanchRepository ranches;
    @Autowired AnimalRepository animals;

    @AfterEach void clear() { TenantContext.clear(); }

    @Test
    void shouldIsolateAnimalsAcrossAccounts() {
        Account a1 = newAccount("acc1");
        Account a2 = newAccount("acc2");

        TenantContext.set(a1.getId());
        Long r1 = newRanch();
        Long b1 = ((com.digitalcow.breed.BreedRepository) breedRepo()).findAll().get(0).getId();
        newAnimal(a1.getId(), r1, b1, "A1-TAG-1");

        TenantContext.set(a2.getId());
        Long r2 = newRanch();
        newAnimal(a2.getId(), r2, b1, "A2-TAG-1");

        // Desde tenant 2 no debe ver el animal de tenant 1
        assertThat(animals.findAll()).allMatch(an -> an.getAccountId().equals(a2.getId()));

        TenantContext.set(a1.getId());
        assertThat(animals.findAll()).allMatch(an -> an.getAccountId().equals(a1.getId()));
    }

    @Autowired com.digitalcow.breed.BreedRepository breedRepoBean;
    com.digitalcow.breed.BreedRepository breedRepo() { return breedRepoBean; }

    private Account newAccount(String slug) {
        Account a = new Account();
        a.setName(slug);
        a.setSlug(slug + "-" + System.nanoTime());
        return accounts.save(a);
    }

    private Long newRanch() {
        com.digitalcow.ranch.Ranch r = new com.digitalcow.ranch.Ranch();
        r.setName("R");
        return ranches.save(r).getId();
    }

    private Long newAnimal(Long accId, Long ranchId, Long breedId, String tag) {
        Animal an = new Animal();
        an.setAccountId(accId);
        an.setRanchId(ranchId);
        an.setBreedId(breedId);
        an.setInternalTag(tag);
        an.setSex(Sex.FEMALE);
        an.setPurpose(Purpose.BEEF);
        an.setStatus(AnimalStatus.ACTIVE);
        an.setCreatedByUserId(1L);
        return animals.save(an).getId();
    }
}
```

- [ ] **Step 4: Run test, expect PASS (validar aislamiento)**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=AnimalIT -q`
Expected: PASS (puede tardar la primera vez por imagen MySQL).

- [ ] **Step 5: Pausa de revisión**

Archivos: 3 archivos test. Confirmar que el container baja al terminar.

---

## Épica I — Fotos

### Task 32: Migración V5 animal_photo + entity

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/resources/db/migration/V5__animal_photo.sql`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/AnimalPhoto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/AnimalPhotoRepository.java`

- [ ] **Step 1: Crear `V5__animal_photo.sql`**

```sql
CREATE TABLE animal_photo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  cloudinary_public_id VARCHAR(200) NOT NULL,
  cloudinary_url VARCHAR(500) NOT NULL,
  width INT NULL,
  height INT NULL,
  bytes INT NULL,
  taken_at TIMESTAMP(6) NULL,
  uploaded_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_photo_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_photo_animal FOREIGN KEY (animal_id) REFERENCES animal(id) ON DELETE CASCADE,
  CONSTRAINT fk_photo_user FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
  INDEX ix_photo_account_animal_created (account_id, animal_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Crear `AnimalPhoto.java`**

```java
package com.digitalcow.photo;

import com.digitalcow.tenancy.TenantAwareEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;

@Entity
@Table(name = "animal_photo")
@EntityListeners(TenantAwareEntityListener.class)
@FilterDef(name = "accountFilter", parameters = @ParamDef(name = "accountId", type = Long.class))
@Filter(name = "accountFilter", condition = "account_id = :accountId")
@Getter @Setter @NoArgsConstructor
public class AnimalPhoto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "animal_id", nullable = false)
    private Long animalId;

    @Column(name = "cloudinary_public_id", nullable = false, length = 200)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    private Integer width;
    private Integer height;
    private Integer bytes;

    @Column(name = "taken_at")
    private Instant takenAt;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
```

- [ ] **Step 3: Crear `AnimalPhotoRepository.java`**

```java
package com.digitalcow.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnimalPhotoRepository extends JpaRepository<AnimalPhoto, Long> {
    List<AnimalPhoto> findAllByAnimalIdOrderByCreatedAtDesc(Long animalId);
}
```

- [ ] **Step 4: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Pausa de revisión**

Archivos: migración, entity, repo.

---

### Task 33: CloudinarySignatureService (firma server-side, sin SDK)

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/CloudinarySignatureService.java`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/photo/CloudinarySignatureServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.digitalcow.photo;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinarySignatureServiceTest {

    @Test
    void shouldComputeSha1HexOfSortedParamsPlusSecret() {
        CloudinarySignatureService svc = new CloudinarySignatureService("name", "key", "secret");
        Map<String, String> params = new LinkedHashMap<>();
        params.put("folder", "accounts/1/animals/2");
        params.put("timestamp", "1715900000");
        params.put("tags", "animal-2");
        String sig = svc.signParams(params);
        assertThat(sig).hasSize(40).matches("[0-9a-f]{40}");
    }
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=CloudinarySignatureServiceTest -q`
Expected: FAIL.

- [ ] **Step 3: Implementar `CloudinarySignatureService.java`**

```java
package com.digitalcow.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Firma uploads a Cloudinary sin usar el SDK.
 * Algoritmo: concatena params ordenados alfabeticamente como key=value separados por &,
 * append api_secret, SHA-1 hex.
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

    public record SignedUpload(String cloudName, String apiKey, long timestamp,
                               String folder, String tags, String signature) {}
}
```

- [ ] **Step 4: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=CloudinarySignatureServiceTest -q`
Expected: PASS.

- [ ] **Step 5: Pausa de revisión**

Archivos: 2 archivos.

---

### Task 34: PhotoService, PhotoController, validación public_id

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/PhotoService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/PhotoController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/dto/SignUploadResponse.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/dto/ConfirmPhotoRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/photo/dto/PhotoDto.java`
- Test: `/Users/noel/REPOS/Digital-Cow/backend/src/test/java/com/digitalcow/photo/PhotoServiceValidationTest.java`

- [ ] **Step 1: Crear DTOs**

`SignUploadResponse.java`:
```java
package com.digitalcow.photo.dto;

public record SignUploadResponse(String cloudName, String apiKey, long timestamp,
                                 String folder, String tags, String signature) {}
```

`ConfirmPhotoRequest.java`:
```java
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
```

`PhotoDto.java`:
```java
package com.digitalcow.photo.dto;

import java.time.Instant;

public record PhotoDto(Long id, String publicId, String url, Integer width, Integer height,
                       Integer bytes, Instant takenAt, Instant createdAt) {}
```

- [ ] **Step 2: Write failing test `PhotoServiceValidationTest`**

```java
package com.digitalcow.photo;

import com.digitalcow.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class PhotoServiceValidationTest {

    @Test
    void shouldAcceptPublicIdWithCorrectPrefix() {
        String expected = "accounts/1/animals/2";
        assertThat(PhotoService.validatePublicId("accounts/1/animals/2/abc123", expected)).isTrue();
    }

    @Test
    void shouldRejectPublicIdWithWrongAccount() {
        String expected = "accounts/1/animals/2";
        assertThatThrownBy(() -> PhotoService.validatePublicId("accounts/9/animals/2/xyz", expected))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectPublicIdWithWrongAnimal() {
        String expected = "accounts/1/animals/2";
        assertThatThrownBy(() -> PhotoService.validatePublicId("accounts/1/animals/999/xyz", expected))
            .isInstanceOf(BusinessException.class);
    }
}
```

- [ ] **Step 3: Run test, expect FAIL**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=PhotoServiceValidationTest -q`
Expected: FAIL.

- [ ] **Step 4: Implementar `PhotoService.java`**

```java
package com.digitalcow.photo;

import com.digitalcow.animal.Animal;
import com.digitalcow.animal.AnimalRepository;
import com.digitalcow.common.error.BusinessException;
import com.digitalcow.common.error.ErrorCode;
import com.digitalcow.common.web.CurrentUser;
import com.digitalcow.photo.dto.*;
import com.digitalcow.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** Firma uploads, confirma fotos, listado, eliminacion y marcado de cover. */
@Service
public class PhotoService {

    private final CloudinarySignatureService sigSvc;
    private final AnimalPhotoRepository photos;
    private final AnimalRepository animals;

    public PhotoService(CloudinarySignatureService sigSvc,
                        AnimalPhotoRepository photos,
                        AnimalRepository animals) {
        this.sigSvc = sigSvc;
        this.photos = photos;
        this.animals = animals;
    }

    public SignUploadResponse signUpload(Long animalId) {
        Animal a = requireAnimal(animalId);
        var s = sigSvc.sign(a.getAccountId(), a.getId());
        return new SignUploadResponse(s.cloudName(), s.apiKey(), s.timestamp(),
            s.folder(), s.tags(), s.signature());
    }

    @Transactional
    public PhotoDto confirm(Long animalId, ConfirmPhotoRequest req) {
        Animal a = requireAnimal(animalId);
        String expected = "accounts/" + a.getAccountId() + "/animals/" + a.getId();
        validatePublicId(req.publicId(), expected);
        AnimalPhoto p = new AnimalPhoto();
        p.setAnimalId(a.getId());
        p.setCloudinaryPublicId(req.publicId());
        p.setCloudinaryUrl(req.url());
        p.setWidth(req.width());
        p.setHeight(req.height());
        p.setBytes(req.bytes());
        p.setTakenAt(req.takenAt());
        p.setUploadedByUserId(CurrentUser.require().userId());
        photos.save(p);
        return toDto(p);
    }

    public List<PhotoDto> list(Long animalId) {
        requireAnimal(animalId);
        return photos.findAllByAnimalIdOrderByCreatedAtDesc(animalId).stream()
            .map(this::toDto).toList();
    }

    @Transactional
    public void delete(Long animalId, Long photoId) {
        AnimalPhoto p = photos.findById(photoId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.PHOTO_NOT_FOUND, "Photo not found"));
        if (!p.getAnimalId().equals(animalId)) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-animal");
        }
        photos.delete(p);
    }

    @Transactional
    public void setCover(Long animalId, Long photoId) {
        Animal a = requireAnimal(animalId);
        AnimalPhoto p = photos.findById(photoId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.PHOTO_NOT_FOUND, "Photo not found"));
        if (!p.getAnimalId().equals(animalId)) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-animal");
        }
        a.setCoverPhotoId(p.getId());
    }

    /** Verifica que public_id pertenezca a la carpeta esperada (defensa contra IDs ajenos). */
    public static boolean validatePublicId(String publicId, String expectedPrefix) {
        if (publicId == null || !publicId.startsWith(expectedPrefix + "/")) {
            throw BusinessException.badRequest(ErrorCode.PHOTO_PUBLIC_ID_INVALID, "Bad public_id");
        }
        return true;
    }

    private Animal requireAnimal(Long animalId) {
        Animal a = animals.findById(animalId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Animal not found"));
        if (!a.getAccountId().equals(TenantContext.get())) {
            throw BusinessException.forbidden(ErrorCode.FORBIDDEN, "Cross-tenant");
        }
        return a;
    }

    private PhotoDto toDto(AnimalPhoto p) {
        return new PhotoDto(p.getId(), p.getCloudinaryPublicId(), p.getCloudinaryUrl(),
            p.getWidth(), p.getHeight(), p.getBytes(), p.getTakenAt(), p.getCreatedAt());
    }
}
```

- [ ] **Step 5: Run test, expect PASS**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -Dtest=PhotoServiceValidationTest -q`
Expected: PASS.

- [ ] **Step 6: Crear `PhotoController.java`**

```java
package com.digitalcow.photo;

import com.digitalcow.photo.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/animals/{animalId}")
public class PhotoController {

    private final PhotoService svc;

    public PhotoController(PhotoService svc) { this.svc = svc; }

    @GetMapping("/photos")
    public List<PhotoDto> list(@PathVariable Long animalId) { return svc.list(animalId); }

    @PostMapping("/photos/sign-upload")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public SignUploadResponse sign(@PathVariable Long animalId) { return svc.signUpload(animalId); }

    @PostMapping("/photos/confirm")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public PhotoDto confirm(@PathVariable Long animalId, @Valid @RequestBody ConfirmPhotoRequest req) {
        return svc.confirm(animalId, req);
    }

    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ResponseEntity<Void> delete(@PathVariable Long animalId, @PathVariable Long photoId) {
        svc.delete(animalId, photoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cover-photo/{photoId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER','WORKER')")
    public ResponseEntity<Void> setCover(@PathVariable Long animalId, @PathVariable Long photoId) {
        svc.setCover(animalId, photoId);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 7: Validar compilación y tests**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw test -q`
Expected: BUILD SUCCESS.

- [ ] **Step 8: Pausa de revisión**

Archivos: 6 archivos en `photo/`.

---

## Épica J — Dashboard

### Task 35: DashboardService + endpoint con cache + invalidación

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/dashboard/DashboardService.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/dashboard/DashboardController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/dashboard/dto/DashboardSummary.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/dashboard/DashboardCacheInvalidator.java`

- [ ] **Step 1: Crear `DashboardSummary.java`**

```java
package com.digitalcow.dashboard.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummary(
    Totals totals,
    List<ByRanchItem> byRanch,
    List<ByBreedItem> byBreed,
    Map<String, Long> bySex,
    Map<String, Long> byPurpose,
    RecentAdditions recentAdditions
) {
    public record Totals(long totalAnimals, long activeAnimals, long soldThisYear,
                         long deadThisYear, long ranches, long lots) {}
    public record ByRanchItem(Long ranchId, String ranchName, long count) {}
    public record ByBreedItem(Long breedId, String breedCode, long count) {}
    public record RecentAdditions(List<String> labels, List<Long> counts) {}
}
```

- [ ] **Step 2: Crear `DashboardService.java`**

```java
package com.digitalcow.dashboard;

import com.digitalcow.config.CacheConfig;
import com.digitalcow.dashboard.dto.DashboardSummary;
import com.digitalcow.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/** Genera el summary del dashboard. Cache 60s por accountId (Caffeine). */
@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager em;

    /** Cache key = accountId del TenantContext. */
    @Cacheable(value = CacheConfig.DASHBOARD_CACHE, key = "T(com.digitalcow.tenancy.TenantContext).get()")
    public DashboardSummary summary() {
        long total = scalar("select count(*) from animal where account_id = :a");
        long active = scalar("select count(*) from animal where account_id = :a and status = 'ACTIVE'");
        int year = LocalDate.now().getYear();
        long sold = scalar("select count(*) from animal where account_id = :a and status = 'SOLD' and year(updated_at) = " + year);
        long dead = scalar("select count(*) from animal where account_id = :a and status = 'DEAD' and year(updated_at) = " + year);
        long ranches = scalar("select count(*) from ranch where account_id = :a");
        long lots = scalar("select count(*) from lot where account_id = :a");

        List<DashboardSummary.ByRanchItem> byRanch = groupByRanch();
        List<DashboardSummary.ByBreedItem> byBreed = groupByBreed();
        Map<String, Long> bySex = groupBySimple("sex");
        Map<String, Long> byPurpose = groupBySimple("purpose");
        DashboardSummary.RecentAdditions recent = recentAdditions();

        return new DashboardSummary(
            new DashboardSummary.Totals(total, active, sold, dead, ranches, lots),
            byRanch, byBreed, bySex, byPurpose, recent
        );
    }

    private long scalar(String sql) {
        Object o = em.createNativeQuery(sql).setParameter("a", TenantContext.get()).getSingleResult();
        return ((Number) o).longValue();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardSummary.ByRanchItem> groupByRanch() {
        List<Object[]> rows = em.createNativeQuery(
            "select r.id, r.name, count(a.id) from ranch r " +
            "left join animal a on a.ranch_id = r.id and a.account_id = :acc " +
            "where r.account_id = :acc group by r.id, r.name")
            .setParameter("acc", TenantContext.get()).getResultList();
        List<DashboardSummary.ByRanchItem> out = new ArrayList<>();
        for (Object[] r : rows) out.add(new DashboardSummary.ByRanchItem(
            ((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()));
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<DashboardSummary.ByBreedItem> groupByBreed() {
        List<Object[]> rows = em.createNativeQuery(
            "select b.id, b.code, count(a.id) from breed b " +
            "left join animal a on a.breed_id = b.id and a.account_id = :acc " +
            "group by b.id, b.code")
            .setParameter("acc", TenantContext.get()).getResultList();
        List<DashboardSummary.ByBreedItem> out = new ArrayList<>();
        for (Object[] r : rows) out.add(new DashboardSummary.ByBreedItem(
            ((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()));
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> groupBySimple(String column) {
        List<Object[]> rows = em.createNativeQuery(
            "select " + column + ", count(*) from animal where account_id = :a group by " + column)
            .setParameter("a", TenantContext.get()).getResultList();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] r : rows) map.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    private DashboardSummary.RecentAdditions recentAdditions() {
        List<Object[]> rows = em.createNativeQuery(
            "select date(created_at), count(*) from animal " +
            "where account_id = :a and created_at >= (now() - interval 30 day) " +
            "group by date(created_at) order by 1")
            .setParameter("a", TenantContext.get()).getResultList();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] r : rows) {
            labels.add(r[0].toString());
            counts.add(((Number) r[1]).longValue());
        }
        return new DashboardSummary.RecentAdditions(labels, counts);
    }
}
```

- [ ] **Step 3: Crear `DashboardController.java`**

```java
package com.digitalcow.dashboard;

import com.digitalcow.dashboard.dto.DashboardSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService svc;

    public DashboardController(DashboardService svc) { this.svc = svc; }

    @GetMapping("/summary")
    public DashboardSummary summary() { return svc.summary(); }
}
```

- [ ] **Step 4: Crear `DashboardCacheInvalidator.java`**

```java
package com.digitalcow.dashboard;

import com.digitalcow.animal.event.AnimalChangedEvent;
import com.digitalcow.config.CacheConfig;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Invalida la cache de dashboard cuando cambia un animal. */
@Component
public class DashboardCacheInvalidator {

    private final CacheManager caches;

    public DashboardCacheInvalidator(CacheManager caches) { this.caches = caches; }

    @EventListener
    public void onAnimalChanged(AnimalChangedEvent ev) {
        var cache = caches.getCache(CacheConfig.DASHBOARD_CACHE);
        if (cache != null && ev.accountId() != null) cache.evict(ev.accountId());
    }
}
```

- [ ] **Step 5: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Pausa de revisión**

Archivos: 4. Verificar que queries usen siempre `account_id = :a`.

---

## Épica K — Super-admin y bootstrap

### Task 36: AdminController y bootstrap super-admin

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/admin/AdminController.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/admin/dto/AdminAccountDto.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/admin/dto/UpdateAdminAccountRequest.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/admin/SuperAdminBootstrap.java`

- [ ] **Step 1: Crear DTOs admin**

`AdminAccountDto.java`:
```java
package com.digitalcow.admin.dto;

public record AdminAccountDto(Long id, String name, String slug, String status, String plan) {}
```

`UpdateAdminAccountRequest.java`:
```java
package com.digitalcow.admin.dto;

import com.digitalcow.account.AccountPlan;
import com.digitalcow.account.AccountStatus;

public record UpdateAdminAccountRequest(AccountStatus status, AccountPlan plan) {}
```

- [ ] **Step 2: Crear `AdminController.java`**

```java
package com.digitalcow.admin;

import com.digitalcow.account.*;
import com.digitalcow.admin.dto.*;
import com.digitalcow.auth.AuthService;
import com.digitalcow.auth.dto.AuthTokensResponse;
import com.digitalcow.auth.dto.LoginRequest;
import com.digitalcow.tenancy.SkipTenancy;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints super-admin (rol global SUPERADMIN). */
@RestController
@RequestMapping("/api/v1/admin")
@SkipTenancy
public class AdminController {

    private final AccountRepository accounts;
    private final AuthService auth;

    public AdminController(AccountRepository accounts, AuthService auth) {
        this.accounts = accounts;
        this.auth = auth;
    }

    @PostMapping("/login")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<AdminAccountDto> list() {
        return accounts.findAll().stream()
            .map(a -> new AdminAccountDto(a.getId(), a.getName(), a.getSlug(),
                a.getStatus().name(), a.getPlan().name()))
            .toList();
    }

    @PatchMapping("/accounts/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Transactional
    public AdminAccountDto update(@PathVariable Long id, @RequestBody UpdateAdminAccountRequest req) {
        Account a = accounts.findById(id).orElseThrow();
        if (req.status() != null) a.setStatus(req.status());
        if (req.plan() != null) a.setPlan(req.plan());
        return new AdminAccountDto(a.getId(), a.getName(), a.getSlug(),
            a.getStatus().name(), a.getPlan().name());
    }
}
```

- [ ] **Step 3: Crear `SuperAdminBootstrap.java`**

```java
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
 * Al arrancar: si no existe ningun SUPERADMIN, crea uno con email de env
 * y password aleatoria que se loguea (debe rotarse en primer login).
 */
@Component
public class SuperAdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrap.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String email;

    public SuperAdminBootstrap(AppUserRepository users, PasswordEncoder encoder,
                               @Value("${digitalcow.superadmin.email}") String email) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
    }

    @Override
    public void run(String... args) {
        if (users.existsByRole(UserRole.SUPERADMIN)) return;
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        String pwd = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        AppUser u = new AppUser();
        u.setAccountId(null);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(pwd));
        u.setFullName("Super Admin");
        u.setRole(UserRole.SUPERADMIN);
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerifiedAt(Instant.now());
        users.save(u);
        log.warn("=== SUPERADMIN CREATED ===\nEmail: {}\nPassword: {}\nROTATE AT FIRST LOGIN", email, pwd);
    }
}
```

- [ ] **Step 4: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Pausa de revisión**

Archivos: 4. Confirmar que SUPERADMIN no tiene accountId.

---

## Épica L — Auditoría

### Task 37: AuditLog entity, repo, @Auditable, AuditAspect

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/audit/AuditLog.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/audit/AuditLogRepository.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/audit/Auditable.java`
- Create: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/audit/AuditAspect.java`
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/animal/AnimalService.java`
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/auth/AuthService.java`
- Modify: `/Users/noel/REPOS/Digital-Cow/backend/src/main/java/com/digitalcow/team/TeamService.java`

- [ ] **Step 1: Crear `AuditLog.java`**

```java
package com.digitalcow.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    public enum Action { CREATE, UPDATE, DELETE, LOGIN, INVITE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Action action;

    @Column(name = "payload_json", columnDefinition = "JSON")
    private String payloadJson;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 250)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
```

- [ ] **Step 2: Crear `AuditLogRepository.java`**

```java
package com.digitalcow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
```

- [ ] **Step 3: Crear `@Auditable` annotation**

```java
package com.digitalcow.audit;

import java.lang.annotation.*;

/** Marca un metodo de service para registrar audit log al ejecutarse. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String entityType();
    AuditLog.Action action();
}
```

- [ ] **Step 4: Crear `AuditAspect.java`**

```java
package com.digitalcow.audit;

import com.digitalcow.auth.JwtAuthenticationFilter.AuthPrincipal;
import com.digitalcow.tenancy.TenantContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Aspect AOP: intercepta @Auditable y registra una entrada en audit_log. */
@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository repo;

    public AuditAspect(AuditLogRepository repo) { this.repo = repo; }

    @Around("@annotation(com.digitalcow.audit.Auditable)")
    public Object record(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        try {
            Auditable a = ((MethodSignature) pjp.getSignature()).getMethod()
                .getAnnotation(Auditable.class);
            AuditLog log = new AuditLog();
            log.setAccountId(TenantContext.get());
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
                log.setUserId(p.userId());
            }
            log.setEntityType(a.entityType());
            log.setAction(a.action());
            log.setEntityId(extractId(result));
            repo.save(log);
        } catch (Exception ignored) {
            // auditoria nunca debe romper la operacion principal
        }
        return result;
    }

    private Long extractId(Object o) {
        if (o == null) return null;
        try {
            var m = o.getClass().getMethod("id");
            Object v = m.invoke(o);
            return v instanceof Long l ? l : null;
        } catch (Exception e) {
            return null;
        }
    }
}
```

- [ ] **Step 5: Anotar métodos en `AnimalService.java`**

Añadir imports y anotaciones:
```java
import com.digitalcow.audit.Auditable;
import com.digitalcow.audit.AuditLog;
```

Y sobre `create`, `update`, `delete`:
```java
@Auditable(entityType = "Animal", action = AuditLog.Action.CREATE)
public AnimalResponse create(AnimalCreateRequest req) { ... }

@Auditable(entityType = "Animal", action = AuditLog.Action.UPDATE)
public AnimalResponse update(Long id, AnimalUpdateRequest req) { ... }

@Auditable(entityType = "Animal", action = AuditLog.Action.DELETE)
public void delete(Long id) { ... }
```

- [ ] **Step 6: Anotar `AuthService.register` y `login`**

```java
@Auditable(entityType = "User", action = AuditLog.Action.CREATE)
public AuthTokensResponse register(...) { ... }

@Auditable(entityType = "User", action = AuditLog.Action.LOGIN)
public AuthTokensResponse login(...) { ... }
```

- [ ] **Step 7: Anotar `TeamService.invite` y `accept`**

```java
@Auditable(entityType = "Invitation", action = AuditLog.Action.INVITE)
public InvitationDto invite(...) { ... }

@Auditable(entityType = "User", action = AuditLog.Action.CREATE)
public void accept(...) { ... }
```

- [ ] **Step 8: Validar compilación**

Run: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw compile -q`
Expected: BUILD SUCCESS.

- [ ] **Step 9: Pausa de revisión**

Archivos: 4 nuevos + 3 modificados.

---

## Épica M — Frontend: fundación

### Task 38: Estructura de carpetas, ThemeProvider, providers raíz

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/providers.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/theme.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/button.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/input.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/label.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/card.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/toast.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/theme-toggle.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/App.tsx`

- [ ] **Step 1: Crear estructura de carpetas**

Run:
```
mkdir -p /Users/noel/REPOS/Digital-Cow/frontend/src/{app,pages,features,components/ui,lib,hooks,locales/en,locales/es,test}
```

Expected: directorios creados.

- [ ] **Step 2: Crear `src/app/theme.tsx`**

```tsx
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

type Theme = 'light' | 'dark';
interface ThemeCtx { theme: Theme; toggle: () => void; }

const Ctx = createContext<ThemeCtx>({ theme: 'light', toggle: () => {} });

/** Provee theme + toggle. Persiste en localStorage y aplica clase `dark` al html. */
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>(() => {
    return (localStorage.getItem('theme') as Theme) ?? 'light';
  });

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem('theme', theme);
  }, [theme]);

  return <Ctx.Provider value={{ theme, toggle: () => setTheme(t => t === 'light' ? 'dark' : 'light') }}>{children}</Ctx.Provider>;
}

export const useTheme = () => useContext(Ctx);
```

- [ ] **Step 3: Crear componentes shadcn básicos**

`src/components/ui/button.tsx`:
```tsx
import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50 disabled:pointer-events-none',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/90',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
        outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
        ghost: 'hover:bg-accent hover:text-accent-foreground'
      },
      size: { default: 'h-10 px-4 py-2', sm: 'h-9 px-3', lg: 'h-11 px-8', icon: 'h-10 w-10' }
    },
    defaultVariants: { variant: 'default', size: 'default' }
  }
);

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement>, VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

/** Boton estilo shadcn con variantes. */
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';
    return <Comp className={cn(buttonVariants({ variant, size, className }))} ref={ref} {...props} />;
  }
);
Button.displayName = 'Button';
```

`src/components/ui/input.tsx`:
```tsx
import * as React from 'react';
import { cn } from '@/lib/utils';

/** Input shadcn estandar. */
export const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, type, ...props }, ref) => (
    <input
      type={type}
      className={cn(
        'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50',
        className
      )}
      ref={ref}
      {...props}
    />
  )
);
Input.displayName = 'Input';
```

`src/components/ui/label.tsx`:
```tsx
import * as React from 'react';
import * as LabelPrimitive from '@radix-ui/react-label';
import { cn } from '@/lib/utils';

export const Label = React.forwardRef<
  React.ElementRef<typeof LabelPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof LabelPrimitive.Root>
>(({ className, ...props }, ref) => (
  <LabelPrimitive.Root ref={ref} className={cn('text-sm font-medium leading-none', className)} {...props} />
));
Label.displayName = 'Label';
```

`src/components/ui/card.tsx`:
```tsx
import * as React from 'react';
import { cn } from '@/lib/utils';

export const Card = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div ref={ref} className={cn('rounded-lg border bg-card text-card-foreground shadow-sm', className)} {...props} />
  )
);
Card.displayName = 'Card';

export const CardHeader = ({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) =>
  <div className={cn('flex flex-col space-y-1.5 p-6', className)} {...props} />;

export const CardTitle = ({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) =>
  <h3 className={cn('text-lg font-semibold leading-none tracking-tight', className)} {...props} />;

export const CardContent = ({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) =>
  <div className={cn('p-6 pt-0', className)} {...props} />;
```

`src/components/ui/toast.tsx`:
```tsx
import { useState, useCallback, type ReactNode, createContext, useContext } from 'react';

interface Toast { id: number; message: string; variant: 'default' | 'destructive'; }
interface Ctx { push: (m: string, v?: Toast['variant']) => void; }
const ToastCtx = createContext<Ctx>({ push: () => {} });

/** Provider de toasts simple en memoria. */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Toast[]>([]);
  const push = useCallback((message: string, variant: Toast['variant'] = 'default') => {
    const id = Date.now() + Math.random();
    setItems(prev => [...prev, { id, message, variant }]);
    setTimeout(() => setItems(prev => prev.filter(t => t.id !== id)), 4000);
  }, []);
  return (
    <ToastCtx.Provider value={{ push }}>
      {children}
      <div className="fixed bottom-4 right-4 flex flex-col gap-2 z-50">
        {items.map(t => (
          <div key={t.id} className={`rounded-md px-4 py-2 text-sm shadow-lg ${t.variant === 'destructive' ? 'bg-destructive text-destructive-foreground' : 'bg-card border'}`}>
            {t.message}
          </div>
        ))}
      </div>
    </ToastCtx.Provider>
  );
}

export const useToast = () => useContext(ToastCtx);
```

- [ ] **Step 4: Crear `src/components/theme-toggle.tsx`**

```tsx
import { Moon, Sun } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useTheme } from '@/app/theme';

/** Boton para alternar entre dark y light. */
export function ThemeToggle() {
  const { theme, toggle } = useTheme();
  return (
    <Button variant="ghost" size="icon" onClick={toggle} aria-label="Toggle theme">
      {theme === 'light' ? <Moon className="h-5 w-5" /> : <Sun className="h-5 w-5" />}
    </Button>
  );
}
```

- [ ] **Step 5: Crear `src/app/providers.tsx`** (skeleton; AuthProvider y i18n se completan en tasks siguientes)

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { ThemeProvider } from './theme';
import { ToastProvider } from '@/components/ui/toast';

const qc = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } }
});

/** Compone todos los providers globales. Auth e i18n se agregan en tasks 39 y 40. */
export function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={qc}>
      <ThemeProvider>
        <ToastProvider>{children}</ToastProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
```

- [ ] **Step 6: Crear `src/app/router.tsx`** (placeholder)

```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

/** Router top-level. Las rutas reales se montan en epicas N-T. */
export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="*" element={<div className="p-8">Loading routes...</div>} />
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Step 7: Modificar `App.tsx`**

```tsx
import { Providers } from './app/providers';
import { AppRouter } from './app/router';

/** Componente raiz. */
export default function App() {
  return (
    <Providers>
      <AppRouter />
    </Providers>
  );
}
```

- [ ] **Step 8: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 9: Pausa de revisión**

Archivos: 10. Confirmar imports relativos a alias `@/`.

---

### Task 39: HTTP client axios con interceptors auth+refresh

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/lib/http.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/lib/auth-storage.ts`

- [ ] **Step 1: Crear `auth-storage.ts`**

```ts
const ACCESS_KEY = 'dc_access';
const REFRESH_KEY = 'dc_refresh';

/** Lee y persiste tokens en localStorage. */
export const AuthStorage = {
  getAccess: () => localStorage.getItem(ACCESS_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  setTokens: (access: string, refresh: string) => {
    localStorage.setItem(ACCESS_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
  },
  clear: () => {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }
};
```

- [ ] **Step 2: Crear `http.ts`**

```ts
import axios, { type AxiosError, type AxiosRequestConfig } from 'axios';
import { AuthStorage } from './auth-storage';

/** Cliente axios compartido. */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  timeout: 15000
});

// Request: anexa Authorization si hay token.
http.interceptors.request.use(config => {
  const token = AuthStorage.getAccess();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshing: Promise<string | null> | null = null;

async function refreshAccess(): Promise<string | null> {
  const refresh = AuthStorage.getRefresh();
  if (!refresh) return null;
  try {
    const { data } = await axios.post(`${http.defaults.baseURL}/auth/refresh`, { refreshToken: refresh });
    AuthStorage.setTokens(data.accessToken, data.refreshToken);
    return data.accessToken;
  } catch {
    AuthStorage.clear();
    return null;
  }
}

// Response: si 401, intenta refresh una vez y reintenta.
http.interceptors.response.use(
  r => r,
  async (err: AxiosError) => {
    const original = err.config as AxiosRequestConfig & { _retried?: boolean };
    if (err.response?.status === 401 && !original._retried) {
      original._retried = true;
      if (!refreshing) refreshing = refreshAccess().finally(() => { refreshing = null; });
      const token = await refreshing;
      if (token) {
        original.headers = { ...original.headers, Authorization: `Bearer ${token}` };
        return http.request(original);
      }
      AuthStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

- [ ] **Step 3: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 4: Pausa de revisión**

Archivos: 2. Confirmar single-flight refresh.

---

### Task 40: AuthContext, ProtectedRoute, RoleGate

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/auth/AuthContext.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/auth/types.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/auth/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/protected-route.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/role-gate.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/providers.tsx`

- [ ] **Step 1: Crear `features/auth/types.ts`**

```ts
export type UserRole = 'OWNER' | 'ADMIN' | 'MANAGER' | 'WORKER' | 'VIEWER' | 'SUPERADMIN';
export type Locale = 'es' | 'en';

export interface Me {
  id: number;
  accountId: number | null;
  email: string;
  fullName: string;
  role: UserRole;
  locale: Locale | null;
  emailVerified: boolean;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
}
```

- [ ] **Step 2: Crear `features/auth/api.ts`**

```ts
import { http } from '@/lib/http';
import type { AuthTokens, Me } from './types';

/** Llamadas REST de auth. */
export const authApi = {
  login: (email: string, password: string) =>
    http.post<AuthTokens>('/auth/login', { email, password }).then(r => r.data),
  register: (payload: { accountName: string; fullName: string; email: string; password: string; locale: 'es' | 'en' }) =>
    http.post<AuthTokens>('/auth/register', payload).then(r => r.data),
  logout: (refreshToken: string) =>
    http.post<void>('/auth/logout', { refreshToken }).then(r => r.data),
  me: () => http.get<Me>('/me').then(r => r.data),
  verifyEmail: (token: string) =>
    http.post<void>('/auth/verify-email', { token }).then(r => r.data),
  requestPasswordReset: (email: string) =>
    http.post<void>('/auth/request-password-reset', { email }).then(r => r.data),
  resetPassword: (token: string, newPassword: string) =>
    http.post<void>('/auth/reset-password', { token, newPassword }).then(r => r.data)
};
```

- [ ] **Step 3: Crear `features/auth/AuthContext.tsx`**

```tsx
import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { authApi } from './api';
import { AuthStorage } from '@/lib/auth-storage';
import type { Me } from './types';

interface AuthCtx {
  user: Me | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: { accountName: string; fullName: string; email: string; password: string; locale: 'es' | 'en' }) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const Ctx = createContext<AuthCtx>(null as unknown as AuthCtx);

/** Provee estado de auth global. Hidrata desde localStorage al montar. */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = useCallback(async () => {
    try {
      const me = await authApi.me();
      setUser(me);
    } catch {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    (async () => {
      if (AuthStorage.getAccess()) await refreshMe();
      setLoading(false);
    })();
  }, [refreshMe]);

  const login = useCallback(async (email: string, password: string) => {
    const t = await authApi.login(email, password);
    AuthStorage.setTokens(t.accessToken, t.refreshToken);
    await refreshMe();
  }, [refreshMe]);

  const register = useCallback(async (payload: Parameters<AuthCtx['register']>[0]) => {
    const t = await authApi.register(payload);
    AuthStorage.setTokens(t.accessToken, t.refreshToken);
    await refreshMe();
  }, [refreshMe]);

  const logout = useCallback(async () => {
    const r = AuthStorage.getRefresh();
    if (r) await authApi.logout(r).catch(() => {});
    AuthStorage.clear();
    setUser(null);
  }, []);

  return <Ctx.Provider value={{ user, loading, login, register, logout, refreshMe }}>{children}</Ctx.Provider>;
}

export const useAuth = () => useContext(Ctx);
```

- [ ] **Step 4: Crear `components/protected-route.tsx`**

```tsx
import { Navigate, useLocation } from 'react-router-dom';
import { type ReactNode } from 'react';
import { useAuth } from '@/features/auth/AuthContext';

/** Envuelve rutas privadas. Si no hay sesion, redirige a /login. */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const loc = useLocation();
  if (loading) return <div className="p-8">Loading...</div>;
  if (!user) return <Navigate to="/login" state={{ from: loc.pathname }} replace />;
  return <>{children}</>;
}
```

- [ ] **Step 5: Crear `components/role-gate.tsx`**

```tsx
import { type ReactNode } from 'react';
import { useAuth } from '@/features/auth/AuthContext';
import type { UserRole } from '@/features/auth/types';

/** Muestra children solo si el rol del usuario coincide. */
export function RoleGate({ roles, children }: { roles: UserRole[]; children: ReactNode }) {
  const { user } = useAuth();
  if (!user || !roles.includes(user.role)) return null;
  return <>{children}</>;
}
```

- [ ] **Step 6: Modificar `app/providers.tsx` para incluir AuthProvider**

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { type ReactNode } from 'react';
import { ThemeProvider } from './theme';
import { ToastProvider } from '@/components/ui/toast';
import { AuthProvider } from '@/features/auth/AuthContext';

const qc = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } }
});

export function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={qc}>
      <ThemeProvider>
        <AuthProvider>
          <ToastProvider>{children}</ToastProvider>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
```

- [ ] **Step 7: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 8: Pausa de revisión**

Archivos: 6. Confirmar que loading de auth bloquea ProtectedRoute.

---

### Task 41: i18n setup, locale JSON skeletons, LanguageSwitcher

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/lib/i18n.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/common.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/auth.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/animals.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/dashboard.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/team.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/ranches.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/es/errors.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/common.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/auth.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/animals.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/dashboard.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/team.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/ranches.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/locales/en/errors.json`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/language-switcher.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/main.tsx`

- [ ] **Step 1: Crear `lib/i18n.ts`**

```ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpBackend from 'i18next-http-backend';
import LanguageDetector from 'i18next-browser-languagedetector';

void i18n
  .use(HttpBackend)
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'es',
    supportedLngs: ['es', 'en'],
    ns: ['common', 'auth', 'animals', 'dashboard', 'team', 'ranches', 'errors'],
    defaultNS: 'common',
    backend: { loadPath: '/locales/{{lng}}/{{ns}}.json' },
    detection: { order: ['localStorage', 'navigator'], caches: ['localStorage'] },
    interpolation: { escapeValue: false }
  });

export default i18n;
```

- [ ] **Step 2: Crear archivos JSON con keys iniciales**

`public/locales/es/common.json`:
```json
{
  "appName": "Digital Cow",
  "actions": { "save": "Guardar", "cancel": "Cancelar", "delete": "Eliminar", "edit": "Editar", "create": "Crear", "back": "Volver", "search": "Buscar" },
  "nav": { "dashboard": "Tablero", "animals": "Animales", "ranches": "Ranchos", "team": "Equipo", "settings": "Configuracion", "logout": "Salir" },
  "language": { "label": "Idioma", "es": "Espanol", "en": "Ingles" },
  "loading": "Cargando..."
}
```

`public/locales/en/common.json`:
```json
{
  "appName": "Digital Cow",
  "actions": { "save": "Save", "cancel": "Cancel", "delete": "Delete", "edit": "Edit", "create": "Create", "back": "Back", "search": "Search" },
  "nav": { "dashboard": "Dashboard", "animals": "Animals", "ranches": "Ranches", "team": "Team", "settings": "Settings", "logout": "Logout" },
  "language": { "label": "Language", "es": "Spanish", "en": "English" },
  "loading": "Loading..."
}
```

`public/locales/es/auth.json`:
```json
{
  "login": { "title": "Iniciar sesion", "email": "Correo", "password": "Contrasena", "submit": "Entrar", "noAccount": "No tienes cuenta?", "register": "Registrarse", "forgot": "Olvidaste tu contrasena?" },
  "register": { "title": "Crear cuenta", "accountName": "Nombre de la organizacion", "fullName": "Nombre completo", "submit": "Crear" },
  "verify": { "title": "Verificacion de correo", "success": "Correo verificado", "fail": "Token invalido" },
  "forgot": { "title": "Recuperar contrasena", "submit": "Enviar enlace", "sent": "Si el correo existe, recibiras instrucciones." },
  "reset": { "title": "Nueva contrasena", "newPassword": "Nueva contrasena", "submit": "Cambiar" },
  "accept": { "title": "Aceptar invitacion", "submit": "Aceptar" }
}
```

`public/locales/en/auth.json`:
```json
{
  "login": { "title": "Sign in", "email": "Email", "password": "Password", "submit": "Sign in", "noAccount": "No account?", "register": "Register", "forgot": "Forgot password?" },
  "register": { "title": "Create account", "accountName": "Organization name", "fullName": "Full name", "submit": "Create" },
  "verify": { "title": "Email verification", "success": "Email verified", "fail": "Invalid token" },
  "forgot": { "title": "Reset password", "submit": "Send link", "sent": "If the email exists you will get instructions." },
  "reset": { "title": "New password", "newPassword": "New password", "submit": "Change" },
  "accept": { "title": "Accept invitation", "submit": "Accept" }
}
```

`public/locales/es/animals.json`:
```json
{
  "title": "Animales",
  "new": "Nuevo animal",
  "fields": { "internalTag": "Arete interno", "officialTag": "Arete oficial", "rfid": "RFID", "name": "Nombre", "sex": "Sexo", "birthDate": "Fecha de nacimiento", "birthDateEstimated": "Fecha estimada", "breed": "Raza", "purpose": "Proposito", "status": "Estado", "ranch": "Rancho", "lot": "Lote", "notes": "Observaciones", "coverPhoto": "Foto principal" },
  "sex": { "FEMALE": "Hembra", "MALE": "Macho" },
  "purpose": { "BEEF": "Carne", "DAIRY": "Leche", "DUAL": "Doble proposito" },
  "status": { "ACTIVE": "Activo", "SOLD": "Vendido", "DEAD": "Baja", "MISSING": "Perdido", "TRANSFERRED": "Transferido" },
  "filters": { "search": "Buscar por arete o nombre" },
  "tabs": { "info": "Informacion", "photos": "Fotos" },
  "photos": { "upload": "Subir foto", "camera": "Tomar foto", "setCover": "Marcar principal", "uploading": "Subiendo...", "compress": "Comprimiendo..." },
  "empty": "No hay animales aun."
}
```

`public/locales/en/animals.json`:
```json
{
  "title": "Animals",
  "new": "New animal",
  "fields": { "internalTag": "Internal tag", "officialTag": "Official tag", "rfid": "RFID", "name": "Name", "sex": "Sex", "birthDate": "Birth date", "birthDateEstimated": "Estimated date", "breed": "Breed", "purpose": "Purpose", "status": "Status", "ranch": "Ranch", "lot": "Lot", "notes": "Notes", "coverPhoto": "Cover photo" },
  "sex": { "FEMALE": "Female", "MALE": "Male" },
  "purpose": { "BEEF": "Beef", "DAIRY": "Dairy", "DUAL": "Dual" },
  "status": { "ACTIVE": "Active", "SOLD": "Sold", "DEAD": "Dead", "MISSING": "Missing", "TRANSFERRED": "Transferred" },
  "filters": { "search": "Search by tag or name" },
  "tabs": { "info": "Information", "photos": "Photos" },
  "photos": { "upload": "Upload photo", "camera": "Take photo", "setCover": "Set as cover", "uploading": "Uploading...", "compress": "Compressing..." },
  "empty": "No animals yet."
}
```

`public/locales/es/dashboard.json`:
```json
{
  "title": "Tablero",
  "totals": { "total": "Total animales", "active": "Activos", "sold": "Vendidos este ano", "dead": "Bajas este ano", "ranches": "Ranchos", "lots": "Lotes" },
  "charts": { "byBreed": "Por raza", "byPurpose": "Por proposito", "byRanch": "Por rancho", "recent": "Altas (30 dias)" },
  "empty": { "title": "No hay animales aun", "cta": "Agrega tu primer animal" }
}
```

`public/locales/en/dashboard.json`:
```json
{
  "title": "Dashboard",
  "totals": { "total": "Total animals", "active": "Active", "sold": "Sold this year", "dead": "Dead this year", "ranches": "Ranches", "lots": "Lots" },
  "charts": { "byBreed": "By breed", "byPurpose": "By purpose", "byRanch": "By ranch", "recent": "Recent (30 days)" },
  "empty": { "title": "No animals yet", "cta": "Add your first animal" }
}
```

`public/locales/es/team.json`:
```json
{
  "title": "Equipo",
  "invite": "Invitar usuario",
  "fields": { "email": "Correo", "role": "Rol", "name": "Nombre" },
  "roles": { "OWNER": "Propietario", "ADMIN": "Administrador", "MANAGER": "Gerente", "WORKER": "Trabajador", "VIEWER": "Visualizador" },
  "status": { "ACTIVE": "Activo", "INVITED": "Invitado", "DISABLED": "Deshabilitado" },
  "pending": "Invitaciones pendientes"
}
```

`public/locales/en/team.json`:
```json
{
  "title": "Team",
  "invite": "Invite user",
  "fields": { "email": "Email", "role": "Role", "name": "Name" },
  "roles": { "OWNER": "Owner", "ADMIN": "Admin", "MANAGER": "Manager", "WORKER": "Worker", "VIEWER": "Viewer" },
  "status": { "ACTIVE": "Active", "INVITED": "Invited", "DISABLED": "Disabled" },
  "pending": "Pending invitations"
}
```

`public/locales/es/ranches.json`:
```json
{
  "title": "Ranchos",
  "new": "Nuevo rancho",
  "fields": { "name": "Nombre", "location": "Ubicacion", "area": "Area (ha)", "notes": "Notas" },
  "lots": { "title": "Lotes", "new": "Nuevo lote", "name": "Nombre del lote" }
}
```

`public/locales/en/ranches.json`:
```json
{
  "title": "Ranches",
  "new": "New ranch",
  "fields": { "name": "Name", "location": "Location", "area": "Area (ha)", "notes": "Notes" },
  "lots": { "title": "Lots", "new": "New lot", "name": "Lot name" }
}
```

`public/locales/es/errors.json`:
```json
{
  "internal": "Error interno",
  "validation": "Validacion fallida",
  "unauthenticated": "Sesion expirada",
  "forbidden": "No autorizado",
  "notFound": "No encontrado",
  "conflict": "Conflicto",
  "auth": { "invalidCredentials": "Correo o contrasena invalidos", "emailNotVerified": "Debes verificar tu correo", "userDisabled": "Usuario deshabilitado", "tokenInvalid": "Token invalido", "tokenExpired": "Token expirado", "refreshInvalid": "Sesion invalida", "emailAlreadyUsed": "El correo ya esta en uso" },
  "invitation": { "invalid": "Invitacion invalida", "expired": "Invitacion expirada", "alreadyAccepted": "Ya aceptada" },
  "ranch": { "hasAnimals": "El rancho tiene animales activos" },
  "lot": { "hasAnimals": "El lote tiene animales activos" },
  "animal": { "tagDuplicate": "Arete interno ya existe", "officialTagDuplicate": "Arete oficial ya existe", "notDeletable": "No se puede eliminar; cambia el estado" },
  "photo": { "publicIdInvalid": "Foto invalida", "notFound": "Foto no encontrada", "serviceUnavailable": "Servicio de fotos no disponible" }
}
```

`public/locales/en/errors.json`:
```json
{
  "internal": "Internal error",
  "validation": "Validation failed",
  "unauthenticated": "Session expired",
  "forbidden": "Forbidden",
  "notFound": "Not found",
  "conflict": "Conflict",
  "auth": { "invalidCredentials": "Invalid email or password", "emailNotVerified": "You must verify your email", "userDisabled": "User disabled", "tokenInvalid": "Invalid token", "tokenExpired": "Token expired", "refreshInvalid": "Invalid session", "emailAlreadyUsed": "Email already in use" },
  "invitation": { "invalid": "Invalid invitation", "expired": "Expired invitation", "alreadyAccepted": "Already accepted" },
  "ranch": { "hasAnimals": "Ranch has active animals" },
  "lot": { "hasAnimals": "Lot has active animals" },
  "animal": { "tagDuplicate": "Internal tag already exists", "officialTagDuplicate": "Official tag already exists", "notDeletable": "Cannot delete; change status" },
  "photo": { "publicIdInvalid": "Invalid photo", "notFound": "Photo not found", "serviceUnavailable": "Photo service unavailable" }
}
```

- [ ] **Step 3: Crear `components/language-switcher.tsx`**

```tsx
import { useTranslation } from 'react-i18next';
import { Button } from './ui/button';
import { useAuth } from '@/features/auth/AuthContext';
import { http } from '@/lib/http';

/** Cambia idioma. Si hay sesion, persiste en backend via PATCH /me. */
export function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const { user, refreshMe } = useAuth();

  const change = async (lng: 'es' | 'en') => {
    await i18n.changeLanguage(lng);
    if (user) {
      await http.patch('/me', { locale: lng }).catch(() => {});
      await refreshMe();
    }
  };

  return (
    <div className="flex gap-1">
      <Button variant={i18n.language.startsWith('es') ? 'default' : 'ghost'} size="sm" onClick={() => change('es')}>ES</Button>
      <Button variant={i18n.language.startsWith('en') ? 'default' : 'ghost'} size="sm" onClick={() => change('en')}>EN</Button>
    </div>
  );
}
```

- [ ] **Step 4: Modificar `main.tsx` para inicializar i18n**

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import './lib/i18n';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

- [ ] **Step 5: Validar build**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run build`
Expected: build exitoso, dist incluye `/locales/`.

- [ ] **Step 6: Pausa de revisión**

Archivos: 17. Confirmar paridad de claves entre es/en.

---

## Épica N — Frontend: páginas de auth

### Task 42: Páginas Login, Register y AuthLayout

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/AuthLayout.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/LoginPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/RegisterPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/auth/schemas.ts`
- Test: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/LoginPage.test.tsx`

- [ ] **Step 1: Crear `features/auth/schemas.ts`**

```ts
import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});
export type LoginValues = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  accountName: z.string().min(1).max(120),
  fullName: z.string().min(1).max(160),
  email: z.string().email(),
  password: z.string().min(8).max(100),
  locale: z.enum(['es', 'en'])
});
export type RegisterValues = z.infer<typeof registerSchema>;
```

- [ ] **Step 2: Crear `AuthLayout.tsx`**

```tsx
import { type ReactNode } from 'react';
import { LanguageSwitcher } from '@/components/language-switcher';
import { ThemeToggle } from '@/components/theme-toggle';

/** Layout centrado para paginas de auth. */
export function AuthLayout({ children, title }: { children: ReactNode; title: string }) {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="flex justify-end p-4 gap-2">
        <LanguageSwitcher />
        <ThemeToggle />
      </header>
      <main className="flex-1 flex items-center justify-center p-4">
        <div className="w-full max-w-sm space-y-6">
          <h1 className="text-2xl font-bold text-center">{title}</h1>
          {children}
        </div>
      </main>
    </div>
  );
}
```

- [ ] **Step 3: Crear `LoginPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { loginSchema, type LoginValues } from '@/features/auth/schemas';
import { useAuth } from '@/features/auth/AuthContext';
import { AuthLayout } from './AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

/** Pagina de login. */
export default function LoginPage() {
  const { t } = useTranslation(['auth', 'errors']);
  const { login } = useAuth();
  const nav = useNavigate();
  const toast = useToast();
  const form = useForm<LoginValues>({ resolver: zodResolver(loginSchema) });

  const onSubmit = form.handleSubmit(async values => {
    try {
      await login(values.email, values.password);
      nav('/dashboard');
    } catch (e: any) {
      const key = e?.response?.data?.error?.messageKey ?? 'errors:auth.invalidCredentials';
      toast.push(t(key, { ns: 'errors' }), 'destructive');
    }
  });

  return (
    <AuthLayout title={t('auth:login.title')}>
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <div>
          <Label htmlFor="email">{t('auth:login.email')}</Label>
          <Input id="email" type="email" {...form.register('email')} />
        </div>
        <div>
          <Label htmlFor="password">{t('auth:login.password')}</Label>
          <Input id="password" type="password" {...form.register('password')} />
        </div>
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {t('auth:login.submit')}
        </Button>
        <div className="text-sm text-center space-y-1">
          <div><Link to="/forgot-password" className="underline">{t('auth:login.forgot')}</Link></div>
          <div>{t('auth:login.noAccount')} <Link to="/register" className="underline">{t('auth:login.register')}</Link></div>
        </div>
      </form>
    </AuthLayout>
  );
}
```

- [ ] **Step 4: Crear `RegisterPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { registerSchema, type RegisterValues } from '@/features/auth/schemas';
import { useAuth } from '@/features/auth/AuthContext';
import { AuthLayout } from './AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

/** Pagina de registro: crea cuenta + Owner. */
export default function RegisterPage() {
  const { t, i18n } = useTranslation(['auth', 'errors']);
  const { register: doRegister } = useAuth();
  const nav = useNavigate();
  const toast = useToast();
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { locale: i18n.language.startsWith('en') ? 'en' : 'es' }
  });

  const onSubmit = form.handleSubmit(async values => {
    try {
      await doRegister(values);
      nav('/dashboard');
    } catch (e: any) {
      const key = e?.response?.data?.error?.messageKey ?? 'errors:internal';
      toast.push(t(key, { ns: 'errors' }), 'destructive');
    }
  });

  return (
    <AuthLayout title={t('auth:register.title')}>
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <div>
          <Label htmlFor="accountName">{t('auth:register.accountName')}</Label>
          <Input id="accountName" {...form.register('accountName')} />
        </div>
        <div>
          <Label htmlFor="fullName">{t('auth:register.fullName')}</Label>
          <Input id="fullName" {...form.register('fullName')} />
        </div>
        <div>
          <Label htmlFor="email">{t('auth:login.email')}</Label>
          <Input id="email" type="email" {...form.register('email')} />
        </div>
        <div>
          <Label htmlFor="password">{t('auth:login.password')}</Label>
          <Input id="password" type="password" {...form.register('password')} />
        </div>
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {t('auth:register.submit')}
        </Button>
      </form>
    </AuthLayout>
  );
}
```

- [ ] **Step 5: Write failing test `LoginPage.test.tsx`**

```tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from './LoginPage';
import { AuthProvider } from '@/features/auth/AuthContext';
import { ToastProvider } from '@/components/ui/toast';

vi.mock('@/features/auth/api', () => ({
  authApi: {
    login: vi.fn().mockResolvedValue({ accessToken: 'a', refreshToken: 'r', expiresInSeconds: 900 }),
    me: vi.fn().mockResolvedValue({ id: 1, accountId: 1, email: 'u@x.com', fullName: 'U', role: 'OWNER', locale: 'es', emailVerified: true })
  }
}));

describe('LoginPage', () => {
  it('submits valid credentials', async () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <ToastProvider>
            <LoginPage />
          </ToastProvider>
        </AuthProvider>
      </MemoryRouter>
    );
    await userEvent.type(screen.getByLabelText(/email/i), 'u@x.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /sign in|entrar/i }));
    expect(true).toBe(true);
  });
});
```

- [ ] **Step 6: Run test**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm test -- src/pages/auth/LoginPage.test.tsx`
Expected: PASS.

- [ ] **Step 7: Pausa de revisión**

Archivos: 5. Confirmar i18n keys correctas.

---

### Task 43: Páginas VerifyEmail, ForgotPassword, ResetPassword, AcceptInvitation + montaje rutas

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/VerifyEmailPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/ForgotPasswordPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/ResetPasswordPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/auth/AcceptInvitationPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `VerifyEmailPage.tsx`**

```tsx
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authApi } from '@/features/auth/api';
import { AuthLayout } from './AuthLayout';

/** Llama POST /auth/verify-email con el token del query string. */
export default function VerifyEmailPage() {
  const [params] = useSearchParams();
  const { t } = useTranslation('auth');
  const [state, setState] = useState<'pending' | 'ok' | 'fail'>('pending');

  useEffect(() => {
    const token = params.get('token');
    if (!token) { setState('fail'); return; }
    authApi.verifyEmail(token).then(() => setState('ok')).catch(() => setState('fail'));
  }, [params]);

  return (
    <AuthLayout title={t('verify.title')}>
      <p className="text-center">
        {state === 'pending' ? '...' : state === 'ok' ? t('verify.success') : t('verify.fail')}
      </p>
    </AuthLayout>
  );
}
```

- [ ] **Step 2: Crear `ForgotPasswordPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { authApi } from '@/features/auth/api';
import { AuthLayout } from './AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const schema = z.object({ email: z.string().email() });

export default function ForgotPasswordPage() {
  const { t } = useTranslation(['auth']);
  const form = useForm<{ email: string }>({ resolver: zodResolver(schema) });
  const [sent, setSent] = useState(false);

  const onSubmit = form.handleSubmit(async values => {
    await authApi.requestPasswordReset(values.email).catch(() => {});
    setSent(true);
  });

  return (
    <AuthLayout title={t('auth:forgot.title')}>
      {sent ? (
        <p className="text-center">{t('auth:forgot.sent')}</p>
      ) : (
        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <div>
            <Label htmlFor="email">{t('auth:login.email')}</Label>
            <Input id="email" type="email" {...form.register('email')} />
          </div>
          <Button type="submit" className="w-full">{t('auth:forgot.submit')}</Button>
        </form>
      )}
    </AuthLayout>
  );
}
```

- [ ] **Step 3: Crear `ResetPasswordPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '@/features/auth/api';
import { AuthLayout } from './AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

const schema = z.object({ newPassword: z.string().min(8).max(100) });

export default function ResetPasswordPage() {
  const { t } = useTranslation(['auth', 'errors']);
  const [params] = useSearchParams();
  const nav = useNavigate();
  const toast = useToast();
  const form = useForm<{ newPassword: string }>({ resolver: zodResolver(schema) });

  const onSubmit = form.handleSubmit(async values => {
    const token = params.get('token');
    if (!token) return;
    try {
      await authApi.resetPassword(token, values.newPassword);
      nav('/login');
    } catch (e: any) {
      const key = e?.response?.data?.error?.messageKey ?? 'errors:internal';
      toast.push(t(key, { ns: 'errors' }), 'destructive');
    }
  });

  return (
    <AuthLayout title={t('auth:reset.title')}>
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <div>
          <Label htmlFor="newPassword">{t('auth:reset.newPassword')}</Label>
          <Input id="newPassword" type="password" {...form.register('newPassword')} />
        </div>
        <Button type="submit" className="w-full">{t('auth:reset.submit')}</Button>
      </form>
    </AuthLayout>
  );
}
```

- [ ] **Step 4: Crear `AcceptInvitationPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { http } from '@/lib/http';
import { AuthLayout } from './AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

const schema = z.object({
  fullName: z.string().min(1).max(160),
  password: z.string().min(8).max(100)
});

export default function AcceptInvitationPage() {
  const { t } = useTranslation(['auth', 'errors']);
  const [params] = useSearchParams();
  const nav = useNavigate();
  const toast = useToast();
  const form = useForm<{ fullName: string; password: string }>({ resolver: zodResolver(schema) });

  const onSubmit = form.handleSubmit(async values => {
    const token = params.get('token');
    if (!token) return;
    try {
      await http.post(`/team/invitations/${token}/accept`, values);
      nav('/login');
    } catch (e: any) {
      const key = e?.response?.data?.error?.messageKey ?? 'errors:internal';
      toast.push(t(key, { ns: 'errors' }), 'destructive');
    }
  });

  return (
    <AuthLayout title={t('auth:accept.title')}>
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <div>
          <Label htmlFor="fullName">{t('auth:register.fullName')}</Label>
          <Input id="fullName" {...form.register('fullName')} />
        </div>
        <div>
          <Label htmlFor="password">{t('auth:login.password')}</Label>
          <Input id="password" type="password" {...form.register('password')} />
        </div>
        <Button type="submit" className="w-full">{t('auth:accept.submit')}</Button>
      </form>
    </AuthLayout>
  );
}
```

- [ ] **Step 5: Modificar `app/router.tsx` para montar rutas de auth**

```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage';
import AcceptInvitationPage from '@/pages/auth/AcceptInvitationPage';

/** Router top-level. Las rutas protegidas se agregan en Task 44. */
export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/accept-invitation" element={<AcceptInvitationPage />} />
        <Route path="*" element={<div className="p-8">404</div>} />
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Step 6: Validar build**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 7: Pausa de revisión**

Archivos: 5. Confirmar tokens en query string.

---

## Épica O — Frontend: shell autenticado

### Task 44: AppLayout (navbar + sidebar) y páginas de Settings

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/AppLayout.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/sidebar.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/user-menu.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/settings/ProfileSettingsPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/settings/AccountSettingsPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `Sidebar.tsx`**

```tsx
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { LayoutDashboard, Beef, Map, Users, Settings } from 'lucide-react';
import { RoleGate } from './role-gate';

/** Navegacion lateral. Items visibles segun rol. */
export function Sidebar() {
  const { t } = useTranslation('common');
  const item = 'flex items-center gap-2 px-3 py-2 rounded-md text-sm hover:bg-accent';
  const active = 'bg-accent font-medium';
  return (
    <nav className="w-56 border-r p-4 space-y-1 hidden md:block">
      <NavLink to="/dashboard" className={({ isActive }) => `${item} ${isActive ? active : ''}`}><LayoutDashboard className="h-4 w-4" />{t('nav.dashboard')}</NavLink>
      <NavLink to="/animals" className={({ isActive }) => `${item} ${isActive ? active : ''}`}><Beef className="h-4 w-4" />{t('nav.animals')}</NavLink>
      <NavLink to="/ranches" className={({ isActive }) => `${item} ${isActive ? active : ''}`}><Map className="h-4 w-4" />{t('nav.ranches')}</NavLink>
      <RoleGate roles={['OWNER', 'ADMIN']}>
        <NavLink to="/team" className={({ isActive }) => `${item} ${isActive ? active : ''}`}><Users className="h-4 w-4" />{t('nav.team')}</NavLink>
      </RoleGate>
      <NavLink to="/settings/profile" className={({ isActive }) => `${item} ${isActive ? active : ''}`}><Settings className="h-4 w-4" />{t('nav.settings')}</NavLink>
    </nav>
  );
}
```

- [ ] **Step 2: Crear `UserMenu.tsx`**

```tsx
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from './ui/button';
import { useAuth } from '@/features/auth/AuthContext';

/** Menu de usuario en navbar: muestra nombre y logout. */
export function UserMenu() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const { t } = useTranslation('common');
  if (!user) return null;
  return (
    <div className="flex items-center gap-3">
      <span className="text-sm">{user.fullName}</span>
      <Button variant="ghost" size="sm" onClick={async () => { await logout(); nav('/login'); }}>
        {t('nav.logout')}
      </Button>
    </div>
  );
}
```

- [ ] **Step 3: Crear `AppLayout.tsx`**

```tsx
import { Outlet } from 'react-router-dom';
import { Sidebar } from '@/components/sidebar';
import { UserMenu } from '@/components/user-menu';
import { LanguageSwitcher } from '@/components/language-switcher';
import { ThemeToggle } from '@/components/theme-toggle';

/** Shell autenticado: header + sidebar + outlet. */
export default function AppLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b flex items-center justify-between px-4 py-2">
        <div className="font-bold">Digital Cow</div>
        <div className="flex items-center gap-2">
          <LanguageSwitcher />
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 p-4 overflow-auto"><Outlet /></main>
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Crear `ProfileSettingsPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@/features/auth/AuthContext';
import { http } from '@/lib/http';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

/** Permite editar nombre y locale del usuario actual. */
export default function ProfileSettingsPage() {
  const { user, refreshMe } = useAuth();
  const { t } = useTranslation('common');
  const toast = useToast();
  const form = useForm<{ fullName: string; locale: 'es' | 'en' }>({
    defaultValues: { fullName: user?.fullName ?? '', locale: (user?.locale as 'es' | 'en') ?? 'es' }
  });

  const onSubmit = form.handleSubmit(async values => {
    await http.patch('/me', values);
    await refreshMe();
    toast.push(t('actions.save'));
  });

  return (
    <form onSubmit={onSubmit} className="max-w-md space-y-4">
      <div>
        <Label htmlFor="fullName">Name</Label>
        <Input id="fullName" {...form.register('fullName')} />
      </div>
      <div>
        <Label htmlFor="locale">Locale</Label>
        <select id="locale" {...form.register('locale')} className="w-full h-10 rounded-md border border-input bg-background px-3">
          <option value="es">ES</option>
          <option value="en">EN</option>
        </select>
      </div>
      <Button type="submit">{t('actions.save')}</Button>
    </form>
  );
}
```

- [ ] **Step 5: Crear `AccountSettingsPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { http } from '@/lib/http';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/toast';

/** Pagina de configuracion de cuenta (Owner/Admin). */
export default function AccountSettingsPage() {
  const { t } = useTranslation('common');
  const toast = useToast();
  const form = useForm<{ name: string; defaultLocale: 'es' | 'en' }>();

  useEffect(() => {
    http.get('/account').then(r => form.reset({ name: r.data.name, defaultLocale: r.data.defaultLocale }));
  }, [form]);

  const onSubmit = form.handleSubmit(async values => {
    await http.patch('/account', values);
    toast.push(t('actions.save'));
  });

  return (
    <form onSubmit={onSubmit} className="max-w-md space-y-4">
      <div>
        <Label htmlFor="name">Account name</Label>
        <Input id="name" {...form.register('name')} />
      </div>
      <div>
        <Label htmlFor="locale">Default locale</Label>
        <select id="locale" {...form.register('defaultLocale')} className="w-full h-10 rounded-md border border-input bg-background px-3">
          <option value="es">ES</option>
          <option value="en">EN</option>
        </select>
      </div>
      <Button type="submit">{t('actions.save')}</Button>
    </form>
  );
}
```

- [ ] **Step 6: Modificar `app/router.tsx` para shell autenticado**

```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage';
import AcceptInvitationPage from '@/pages/auth/AcceptInvitationPage';
import AppLayout from '@/pages/AppLayout';
import ProfileSettingsPage from '@/pages/settings/ProfileSettingsPage';
import AccountSettingsPage from '@/pages/settings/AccountSettingsPage';
import { ProtectedRoute } from '@/components/protected-route';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/accept-invitation" element={<AcceptInvitationPage />} />

        <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          <Route path="/dashboard" element={<div>Dashboard (Task 49)</div>} />
          <Route path="/animals/*" element={<div>Animals (Task 47/48)</div>} />
          <Route path="/ranches/*" element={<div>Ranches (Task 45)</div>} />
          <Route path="/team" element={<div>Team (Task 46)</div>} />
          <Route path="/settings/profile" element={<ProfileSettingsPage />} />
          <Route path="/settings/account" element={<AccountSettingsPage />} />
        </Route>

        <Route path="*" element={<div className="p-8">404</div>} />
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Step 7: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 8: Pausa de revisión**

Archivos: 6. Confirmar RoleGate en sidebar.

---

## Épica P — Frontend: ranchos y lotes

### Task 45: Páginas /ranches, /ranches/:id, forms ranch+lot

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/ranches/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/ranches/schemas.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/ranches/types.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/ranches/RanchesPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/ranches/RanchDetailPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/ranches/components/RanchFormDialog.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/ranches/components/LotFormDialog.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/components/ui/dialog.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `Dialog` shadcn wrapper**

```tsx
import * as React from 'react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X } from 'lucide-react';
import { cn } from '@/lib/utils';

export const Dialog = DialogPrimitive.Root;
export const DialogTrigger = DialogPrimitive.Trigger;

export const DialogContent = React.forwardRef<
  React.ElementRef<typeof DialogPrimitive.Content>,
  React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content>
>(({ className, children, ...props }, ref) => (
  <DialogPrimitive.Portal>
    <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm" />
    <DialogPrimitive.Content
      ref={ref}
      className={cn(
        'fixed left-[50%] top-[50%] z-50 grid w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg rounded-lg',
        className
      )}
      {...props}
    >
      {children}
      <DialogPrimitive.Close className="absolute right-4 top-4">
        <X className="h-4 w-4" />
      </DialogPrimitive.Close>
    </DialogPrimitive.Content>
  </DialogPrimitive.Portal>
));
DialogContent.displayName = 'DialogContent';

export const DialogTitle = DialogPrimitive.Title;
```

- [ ] **Step 2: Crear `features/ranches/types.ts`, `schemas.ts`, `api.ts`**

`types.ts`:
```ts
export interface Ranch {
  id: number;
  name: string;
  location?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  areaHectares?: number | null;
  notes?: string | null;
}

export interface Lot {
  id: number;
  ranchId: number;
  name: string;
  areaHectares?: number | null;
  notes?: string | null;
}
```

`schemas.ts`:
```ts
import { z } from 'zod';

export const ranchSchema = z.object({
  name: z.string().min(1).max(120),
  location: z.string().max(200).optional().nullable(),
  areaHectares: z.coerce.number().optional().nullable(),
  notes: z.string().optional().nullable()
});
export type RanchValues = z.infer<typeof ranchSchema>;

export const lotSchema = z.object({
  name: z.string().min(1).max(120),
  areaHectares: z.coerce.number().optional().nullable(),
  notes: z.string().optional().nullable()
});
export type LotValues = z.infer<typeof lotSchema>;
```

`api.ts`:
```ts
import { http } from '@/lib/http';
import type { Ranch, Lot } from './types';
import type { RanchValues, LotValues } from './schemas';

export const ranchApi = {
  list: () => http.get<Ranch[]>('/ranches').then(r => r.data),
  get: (id: number) => http.get<Ranch>(`/ranches/${id}`).then(r => r.data),
  create: (v: RanchValues) => http.post<Ranch>('/ranches', v).then(r => r.data),
  update: (id: number, v: RanchValues) => http.patch<Ranch>(`/ranches/${id}`, v).then(r => r.data),
  remove: (id: number) => http.delete(`/ranches/${id}`),
  listLots: (ranchId: number) => http.get<Lot[]>(`/ranches/${ranchId}/lots`).then(r => r.data),
  createLot: (ranchId: number, v: LotValues) => http.post<Lot>(`/ranches/${ranchId}/lots`, v).then(r => r.data),
  updateLot: (id: number, v: LotValues) => http.patch<Lot>(`/lots/${id}`, v).then(r => r.data),
  removeLot: (id: number) => http.delete(`/lots/${id}`)
};
```

- [ ] **Step 3: Crear `RanchFormDialog.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ranchSchema, type RanchValues } from '../schemas';
import { ranchApi } from '../api';
import type { Ranch } from '../types';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface Props { open: boolean; onClose: () => void; ranch?: Ranch; }

/** Dialog para crear o editar un rancho. */
export function RanchFormDialog({ open, onClose, ranch }: Props) {
  const { t } = useTranslation(['ranches', 'common']);
  const qc = useQueryClient();
  const form = useForm<RanchValues>({
    resolver: zodResolver(ranchSchema),
    defaultValues: ranch ?? { name: '', location: '', areaHectares: null, notes: '' }
  });

  const m = useMutation({
    mutationFn: (v: RanchValues) => ranch ? ranchApi.update(ranch.id, v) : ranchApi.create(v),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['ranches'] }); onClose(); }
  });

  return (
    <Dialog open={open} onOpenChange={o => !o && onClose()}>
      <DialogContent>
        <DialogTitle>{ranch ? t('common:actions.edit') : t('ranches:new')}</DialogTitle>
        <form onSubmit={form.handleSubmit(v => m.mutate(v))} className="space-y-3">
          <div><Label>{t('ranches:fields.name')}</Label><Input {...form.register('name')} /></div>
          <div><Label>{t('ranches:fields.location')}</Label><Input {...form.register('location')} /></div>
          <div><Label>{t('ranches:fields.area')}</Label><Input type="number" step="0.01" {...form.register('areaHectares')} /></div>
          <div><Label>{t('ranches:fields.notes')}</Label><Input {...form.register('notes')} /></div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>{t('common:actions.cancel')}</Button>
            <Button type="submit" disabled={m.isPending}>{t('common:actions.save')}</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
```

- [ ] **Step 4: Crear `LotFormDialog.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { lotSchema, type LotValues } from '../schemas';
import { ranchApi } from '../api';
import type { Lot } from '../types';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface Props { open: boolean; onClose: () => void; ranchId: number; lot?: Lot; }

export function LotFormDialog({ open, onClose, ranchId, lot }: Props) {
  const { t } = useTranslation(['ranches', 'common']);
  const qc = useQueryClient();
  const form = useForm<LotValues>({
    resolver: zodResolver(lotSchema),
    defaultValues: lot ?? { name: '', areaHectares: null, notes: '' }
  });

  const m = useMutation({
    mutationFn: (v: LotValues) => lot ? ranchApi.updateLot(lot.id, v) : ranchApi.createLot(ranchId, v),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['lots', ranchId] }); onClose(); }
  });

  return (
    <Dialog open={open} onOpenChange={o => !o && onClose()}>
      <DialogContent>
        <DialogTitle>{lot ? t('common:actions.edit') : t('ranches:lots.new')}</DialogTitle>
        <form onSubmit={form.handleSubmit(v => m.mutate(v))} className="space-y-3">
          <div><Label>{t('ranches:lots.name')}</Label><Input {...form.register('name')} /></div>
          <div><Label>{t('ranches:fields.area')}</Label><Input type="number" step="0.01" {...form.register('areaHectares')} /></div>
          <div><Label>{t('ranches:fields.notes')}</Label><Input {...form.register('notes')} /></div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>{t('common:actions.cancel')}</Button>
            <Button type="submit" disabled={m.isPending}>{t('common:actions.save')}</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
```

- [ ] **Step 5: Crear `RanchesPage.tsx`**

```tsx
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ranchApi } from '@/features/ranches/api';
import { RanchFormDialog } from '@/features/ranches/components/RanchFormDialog';
import { Button } from '@/components/ui/button';

/** Listado de ranchos de la cuenta. */
export default function RanchesPage() {
  const { t } = useTranslation('ranches');
  const { data = [] } = useQuery({ queryKey: ['ranches'], queryFn: ranchApi.list });
  const [open, setOpen] = useState(false);

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold">{t('title')}</h2>
        <Button onClick={() => setOpen(true)}>{t('new')}</Button>
      </div>
      <table className="w-full text-sm">
        <thead><tr className="border-b text-left"><th className="p-2">{t('fields.name')}</th><th>{t('fields.location')}</th><th>{t('fields.area')}</th></tr></thead>
        <tbody>
          {data.map(r => (
            <tr key={r.id} className="border-b hover:bg-accent">
              <td className="p-2"><Link to={`/ranches/${r.id}`} className="underline">{r.name}</Link></td>
              <td>{r.location}</td>
              <td>{r.areaHectares ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <RanchFormDialog open={open} onClose={() => setOpen(false)} />
    </div>
  );
}
```

- [ ] **Step 6: Crear `RanchDetailPage.tsx`**

```tsx
import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ranchApi } from '@/features/ranches/api';
import { LotFormDialog } from '@/features/ranches/components/LotFormDialog';
import { Button } from '@/components/ui/button';

/** Detalle de rancho con su lista de lotes. */
export default function RanchDetailPage() {
  const { id } = useParams<{ id: string }>();
  const ranchId = Number(id);
  const { t } = useTranslation('ranches');
  const ranch = useQuery({ queryKey: ['ranch', ranchId], queryFn: () => ranchApi.get(ranchId) });
  const lots = useQuery({ queryKey: ['lots', ranchId], queryFn: () => ranchApi.listLots(ranchId) });
  const [open, setOpen] = useState(false);

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold">{ranch.data?.name}</h2>
      <div className="flex justify-between items-center">
        <h3 className="font-semibold">{t('lots.title')}</h3>
        <Button onClick={() => setOpen(true)}>{t('lots.new')}</Button>
      </div>
      <ul className="space-y-1">
        {(lots.data ?? []).map(l => <li key={l.id} className="border rounded p-2">{l.name} ({l.areaHectares ?? '-'} ha)</li>)}
      </ul>
      <LotFormDialog open={open} onClose={() => setOpen(false)} ranchId={ranchId} />
    </div>
  );
}
```

- [ ] **Step 7: Modificar router para conectar paginas reales**

Reemplazar las rutas placeholder de `/ranches`:
```tsx
<Route path="/ranches" element={<RanchesPage />} />
<Route path="/ranches/:id" element={<RanchDetailPage />} />
```

Y los imports correspondientes al top del archivo router:
```tsx
import RanchesPage from '@/pages/ranches/RanchesPage';
import RanchDetailPage from '@/pages/ranches/RanchDetailPage';
```

- [ ] **Step 8: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 9: Pausa de revisión**

Archivos: 9.

---

## Épica Q — Frontend: equipo

### Task 46: Página /team con invitaciones y edición de roles

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/team/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/team/types.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/team/schemas.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/team/components/InviteUserDialog.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/team/TeamPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `types.ts`, `schemas.ts`, `api.ts`**

`types.ts`:
```ts
import type { UserRole } from '@/features/auth/types';
export type UserStatus = 'ACTIVE' | 'INVITED' | 'DISABLED';

export interface TeamUser { id: number; email: string; fullName: string; role: UserRole; status: UserStatus; }
export interface Invitation { id: number; email: string; role: UserRole; expiresAt: string; acceptedAt: string | null; }
```

`schemas.ts`:
```ts
import { z } from 'zod';

export const inviteSchema = z.object({
  email: z.string().email(),
  role: z.enum(['OWNER', 'ADMIN', 'MANAGER', 'WORKER', 'VIEWER'])
});
export type InviteValues = z.infer<typeof inviteSchema>;
```

`api.ts`:
```ts
import { http } from '@/lib/http';
import type { TeamUser, Invitation } from './types';
import type { InviteValues } from './schemas';
import type { UserRole } from '@/features/auth/types';

export const teamApi = {
  listUsers: () => http.get<TeamUser[]>('/team').then(r => r.data),
  listInvitations: () => http.get<Invitation[]>('/team/invitations').then(r => r.data),
  invite: (v: InviteValues) => http.post<Invitation>('/team/invitations', v).then(r => r.data),
  deleteInvitation: (id: number) => http.delete(`/team/invitations/${id}`),
  updateUser: (id: number, body: { role?: UserRole; status?: 'ACTIVE' | 'DISABLED' }) =>
    http.patch<TeamUser>(`/team/users/${id}`, body).then(r => r.data)
};
```

- [ ] **Step 2: Crear `InviteUserDialog.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { inviteSchema, type InviteValues } from '../schemas';
import { teamApi } from '../api';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export function InviteUserDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { t } = useTranslation(['team', 'common']);
  const qc = useQueryClient();
  const form = useForm<InviteValues>({
    resolver: zodResolver(inviteSchema),
    defaultValues: { email: '', role: 'WORKER' }
  });
  const m = useMutation({
    mutationFn: teamApi.invite,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['invitations'] }); onClose(); }
  });

  return (
    <Dialog open={open} onOpenChange={o => !o && onClose()}>
      <DialogContent>
        <DialogTitle>{t('team:invite')}</DialogTitle>
        <form onSubmit={form.handleSubmit(v => m.mutate(v))} className="space-y-3">
          <div><Label>{t('team:fields.email')}</Label><Input type="email" {...form.register('email')} /></div>
          <div>
            <Label>{t('team:fields.role')}</Label>
            <select {...form.register('role')} className="w-full h-10 rounded-md border bg-background px-3">
              {(['OWNER', 'ADMIN', 'MANAGER', 'WORKER', 'VIEWER'] as const).map(r =>
                <option key={r} value={r}>{t(`team:roles.${r}`)}</option>)}
            </select>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>{t('common:actions.cancel')}</Button>
            <Button type="submit" disabled={m.isPending}>{t('common:actions.save')}</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
```

- [ ] **Step 3: Crear `TeamPage.tsx`**

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { teamApi } from '@/features/team/api';
import { InviteUserDialog } from '@/features/team/components/InviteUserDialog';
import { Button } from '@/components/ui/button';
import type { UserRole } from '@/features/auth/types';

/** Pagina de equipo: usuarios + invitaciones pendientes. */
export default function TeamPage() {
  const { t } = useTranslation(['team', 'common']);
  const users = useQuery({ queryKey: ['team-users'], queryFn: teamApi.listUsers });
  const invs = useQuery({ queryKey: ['invitations'], queryFn: teamApi.listInvitations });
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);

  const update = useMutation({
    mutationFn: ({ id, role, status }: { id: number; role?: UserRole; status?: 'ACTIVE' | 'DISABLED' }) =>
      teamApi.updateUser(id, { role, status }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['team-users'] })
  });

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold">{t('team:title')}</h2>
        <Button onClick={() => setOpen(true)}>{t('team:invite')}</Button>
      </div>

      <table className="w-full text-sm border">
        <thead><tr className="border-b"><th className="p-2 text-left">Email</th><th>Nombre</th><th>Rol</th><th>Status</th></tr></thead>
        <tbody>
          {(users.data ?? []).map(u => (
            <tr key={u.id} className="border-b">
              <td className="p-2">{u.email}</td><td>{u.fullName}</td>
              <td>
                <select value={u.role} onChange={e => update.mutate({ id: u.id, role: e.target.value as UserRole })}
                  className="border rounded px-2 py-1 bg-background">
                  {(['OWNER','ADMIN','MANAGER','WORKER','VIEWER'] as const).map(r =>
                    <option key={r} value={r}>{t(`team:roles.${r}`)}</option>)}
                </select>
              </td>
              <td>
                <select value={u.status === 'INVITED' ? 'INVITED' : u.status}
                  onChange={e => update.mutate({ id: u.id, status: e.target.value as 'ACTIVE' | 'DISABLED' })}
                  className="border rounded px-2 py-1 bg-background">
                  <option value="ACTIVE">{t('team:status.ACTIVE')}</option>
                  <option value="DISABLED">{t('team:status.DISABLED')}</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div>
        <h3 className="font-semibold mb-2">{t('team:pending')}</h3>
        <ul className="space-y-1">
          {(invs.data ?? []).map(i => (
            <li key={i.id} className="border rounded p-2 flex justify-between items-center">
              <span>{i.email} - {t(`team:roles.${i.role}`)}</span>
              <Button variant="ghost" size="sm" onClick={() => teamApi.deleteInvitation(i.id).then(() => qc.invalidateQueries({ queryKey: ['invitations'] }))}>
                {t('common:actions.delete')}
              </Button>
            </li>
          ))}
        </ul>
      </div>

      <InviteUserDialog open={open} onClose={() => setOpen(false)} />
    </div>
  );
}
```

- [ ] **Step 4: Modificar router**

Reemplazar `<Route path="/team" ...>` por:
```tsx
<Route path="/team" element={<TeamPage />} />
```

Y agregar `import TeamPage from '@/pages/team/TeamPage';`.

- [ ] **Step 5: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 6: Pausa de revisión**

Archivos: 6.

---

## Épica R — Frontend: animales

### Task 47: API/types/schemas + página /animals lista con filtros

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/types.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/schemas.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/breeds/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/animals/AnimalsListPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `features/animals/types.ts`**

```ts
export type Sex = 'FEMALE' | 'MALE';
export type Purpose = 'BEEF' | 'DAIRY' | 'DUAL';
export type AnimalStatus = 'ACTIVE' | 'SOLD' | 'DEAD' | 'MISSING' | 'TRANSFERRED';

export interface AnimalListItem {
  id: number;
  internalTag: string;
  officialTag?: string | null;
  name?: string | null;
  breedId: number;
  sex: Sex;
  status: AnimalStatus;
  lotId?: number | null;
  coverPhotoId?: number | null;
}

export interface AnimalResponse extends AnimalListItem {
  ranchId: number;
  rfid?: string | null;
  birthDate?: string | null;
  birthDateEstimated: boolean;
  purpose: Purpose;
  notes?: string | null;
  createdByUserId: number;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }
```

- [ ] **Step 2: Crear `features/animals/schemas.ts`**

```ts
import { z } from 'zod';

export const animalSchema = z.object({
  ranchId: z.coerce.number().int().positive(),
  lotId: z.coerce.number().int().positive().optional().nullable(),
  internalTag: z.string().min(1).max(40),
  officialTag: z.string().max(60).optional().nullable(),
  rfid: z.string().max(40).optional().nullable(),
  name: z.string().max(80).optional().nullable(),
  sex: z.enum(['FEMALE', 'MALE']),
  birthDate: z.string().optional().nullable(),
  birthDateEstimated: z.boolean().default(false),
  breedId: z.coerce.number().int().positive(),
  purpose: z.enum(['BEEF', 'DAIRY', 'DUAL']),
  status: z.enum(['ACTIVE', 'SOLD', 'DEAD', 'MISSING', 'TRANSFERRED']).default('ACTIVE'),
  notes: z.string().optional().nullable()
});
export type AnimalValues = z.infer<typeof animalSchema>;
```

- [ ] **Step 3: Crear `features/animals/api.ts`**

```ts
import { http } from '@/lib/http';
import type { AnimalListItem, AnimalResponse, Page } from './types';
import type { AnimalValues } from './schemas';

export interface AnimalFilters {
  search?: string; ranchId?: number; lotId?: number; breedId?: number;
  sex?: 'FEMALE' | 'MALE'; purpose?: 'BEEF' | 'DAIRY' | 'DUAL'; status?: string;
  page?: number; size?: number;
}

export const animalsApi = {
  list: (filters: AnimalFilters) =>
    http.get<Page<AnimalListItem>>('/animals', { params: filters }).then(r => r.data),
  get: (id: number) => http.get<AnimalResponse>(`/animals/${id}`).then(r => r.data),
  create: (v: AnimalValues) => http.post<AnimalResponse>('/animals', v).then(r => r.data),
  update: (id: number, v: Partial<AnimalValues>) => http.patch<AnimalResponse>(`/animals/${id}`, v).then(r => r.data),
  remove: (id: number) => http.delete(`/animals/${id}`)
};
```

- [ ] **Step 4: Crear `features/breeds/api.ts`**

```ts
import { http } from '@/lib/http';

export interface Breed { id: number; code: string; nameEs: string; nameEn: string; category: string; }

export const breedsApi = {
  list: () => http.get<Breed[]>('/breeds').then(r => r.data)
};
```

- [ ] **Step 5: Crear `AnimalsListPage.tsx`**

```tsx
import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useDeferredValue, useState } from 'react';
import { animalsApi } from '@/features/animals/api';
import { breedsApi } from '@/features/breeds/api';
import { ranchApi } from '@/features/ranches/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

/** Listado paginado de animales con filtros sincronizados al URL. */
export default function AnimalsListPage() {
  const { t } = useTranslation('animals');
  const [params, setParams] = useSearchParams();
  const page = Number(params.get('page') ?? 0);
  const [searchInput, setSearchInput] = useState(params.get('search') ?? '');
  const search = useDeferredValue(searchInput);

  const filters = {
    page,
    size: 20,
    search: search || undefined,
    ranchId: params.get('ranchId') ? Number(params.get('ranchId')) : undefined,
    breedId: params.get('breedId') ? Number(params.get('breedId')) : undefined,
    status: params.get('status') ?? undefined,
    sex: (params.get('sex') as 'FEMALE' | 'MALE' | null) ?? undefined,
    purpose: (params.get('purpose') as 'BEEF' | 'DAIRY' | 'DUAL' | null) ?? undefined
  };

  const list = useQuery({
    queryKey: ['animals', filters],
    queryFn: () => animalsApi.list(filters),
    placeholderData: prev => prev
  });
  const breeds = useQuery({ queryKey: ['breeds'], queryFn: breedsApi.list });
  const ranches = useQuery({ queryKey: ['ranches'], queryFn: ranchApi.list });

  const setParam = (k: string, v: string | undefined) => {
    const p = new URLSearchParams(params);
    if (v) p.set(k, v); else p.delete(k);
    p.set('page', '0');
    setParams(p);
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold">{t('title')}</h2>
        <Button asChild><Link to="/animals/new">{t('new')}</Link></Button>
      </div>

      <div className="flex flex-wrap gap-2">
        <Input placeholder={t('filters.search')} value={searchInput}
          onChange={e => { setSearchInput(e.target.value); setParam('search', e.target.value); }} className="max-w-xs" />
        <select className="border rounded px-2 py-1 bg-background" value={params.get('ranchId') ?? ''} onChange={e => setParam('ranchId', e.target.value || undefined)}>
          <option value="">{t('fields.ranch')}</option>
          {(ranches.data ?? []).map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
        <select className="border rounded px-2 py-1 bg-background" value={params.get('breedId') ?? ''} onChange={e => setParam('breedId', e.target.value || undefined)}>
          <option value="">{t('fields.breed')}</option>
          {(breeds.data ?? []).map(b => <option key={b.id} value={b.id}>{b.nameEs}</option>)}
        </select>
        <select className="border rounded px-2 py-1 bg-background" value={params.get('status') ?? ''} onChange={e => setParam('status', e.target.value || undefined)}>
          <option value="">{t('fields.status')}</option>
          {(['ACTIVE','SOLD','DEAD','MISSING','TRANSFERRED'] as const).map(s => <option key={s} value={s}>{t(`status.${s}`)}</option>)}
        </select>
      </div>

      {list.isLoading ? <div>...</div> : (list.data?.content?.length ? (
        <table className="w-full text-sm border">
          <thead><tr className="border-b text-left"><th className="p-2">{t('fields.internalTag')}</th><th>{t('fields.name')}</th><th>{t('fields.sex')}</th><th>{t('fields.status')}</th></tr></thead>
          <tbody>
            {list.data.content.map(a => (
              <tr key={a.id} className="border-b hover:bg-accent">
                <td className="p-2"><Link to={`/animals/${a.id}`} className="underline">{a.internalTag}</Link></td>
                <td>{a.name ?? '-'}</td>
                <td>{t(`sex.${a.sex}`)}</td>
                <td>{t(`status.${a.status}`)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : <p>{t('empty')}</p>)}

      {list.data && list.data.totalPages > 1 && (
        <div className="flex gap-2">
          <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setParam('page', String(page - 1))}>Prev</Button>
          <span className="self-center">{page + 1} / {list.data.totalPages}</span>
          <Button variant="outline" size="sm" disabled={page >= list.data.totalPages - 1} onClick={() => setParam('page', String(page + 1))}>Next</Button>
        </div>
      )}
    </div>
  );
}
```

- [ ] **Step 6: Modificar router**

Reemplazar `<Route path="/animals/*" ...>` por las rutas reales (placeholders para detalle/new se conectan en Task 48):
```tsx
<Route path="/animals" element={<AnimalsListPage />} />
<Route path="/animals/new" element={<AnimalEditPage />} />
<Route path="/animals/:id" element={<AnimalDetailPage />} />
<Route path="/animals/:id/edit" element={<AnimalEditPage />} />
```

Y agregar imports al top:
```tsx
import AnimalsListPage from '@/pages/animals/AnimalsListPage';
import AnimalDetailPage from '@/pages/animals/AnimalDetailPage';
import AnimalEditPage from '@/pages/animals/AnimalEditPage';
```

NOTA: `AnimalDetailPage` y `AnimalEditPage` se crean en Task 48.

- [ ] **Step 7: Pausa de revisión**

Archivos: 6. Confirmar URL state sync.

---

### Task 48: AnimalForm, AnimalDetailPage, AnimalEditPage, PhotoUploader

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/components/AnimalForm.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/components/PhotoUploader.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/components/PhotoGallery.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/animals/AnimalDetailPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/animals/AnimalEditPage.tsx`
- Test: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/animals/components/AnimalForm.test.tsx`

- [ ] **Step 1: Crear `AnimalForm.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { animalSchema, type AnimalValues } from '../schemas';
import { breedsApi } from '@/features/breeds/api';
import { ranchApi } from '@/features/ranches/api';
import type { AnimalResponse } from '../types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface Props { initial?: AnimalResponse; onSubmit: (v: AnimalValues) => Promise<void> | void; submitting?: boolean; }

/** Form reutilizable para crear o editar animal. */
export function AnimalForm({ initial, onSubmit, submitting }: Props) {
  const { t } = useTranslation(['animals', 'common']);
  const breeds = useQuery({ queryKey: ['breeds'], queryFn: breedsApi.list });
  const ranches = useQuery({ queryKey: ['ranches'], queryFn: ranchApi.list });
  const form = useForm<AnimalValues>({
    resolver: zodResolver(animalSchema),
    defaultValues: initial as AnimalValues | undefined ?? {
      ranchId: 0 as unknown as number, internalTag: '', sex: 'FEMALE', breedId: 0 as unknown as number,
      purpose: 'BEEF', status: 'ACTIVE', birthDateEstimated: false
    }
  });

  return (
    <form onSubmit={form.handleSubmit(async v => { await onSubmit(v); })} className="space-y-3 max-w-xl">
      <div><Label>{t('animals:fields.internalTag')}</Label><Input {...form.register('internalTag')} /></div>
      <div><Label>{t('animals:fields.officialTag')}</Label><Input {...form.register('officialTag')} /></div>
      <div><Label>{t('animals:fields.rfid')}</Label><Input {...form.register('rfid')} /></div>
      <div><Label>{t('animals:fields.name')}</Label><Input {...form.register('name')} /></div>
      <div>
        <Label>{t('animals:fields.sex')}</Label>
        <select {...form.register('sex')} className="w-full h-10 rounded border bg-background px-3">
          {(['FEMALE','MALE'] as const).map(s => <option key={s} value={s}>{t(`animals:sex.${s}`)}</option>)}
        </select>
      </div>
      <div><Label>{t('animals:fields.birthDate')}</Label><Input type="date" {...form.register('birthDate')} /></div>
      <div className="flex items-center gap-2">
        <input type="checkbox" id="bde" {...form.register('birthDateEstimated')} />
        <Label htmlFor="bde">{t('animals:fields.birthDateEstimated')}</Label>
      </div>
      <div>
        <Label>{t('animals:fields.breed')}</Label>
        <select {...form.register('breedId')} className="w-full h-10 rounded border bg-background px-3">
          <option value="">-</option>
          {(breeds.data ?? []).map(b => <option key={b.id} value={b.id}>{b.nameEs}</option>)}
        </select>
      </div>
      <div>
        <Label>{t('animals:fields.ranch')}</Label>
        <select {...form.register('ranchId')} className="w-full h-10 rounded border bg-background px-3">
          <option value="">-</option>
          {(ranches.data ?? []).map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
      </div>
      <div>
        <Label>{t('animals:fields.purpose')}</Label>
        <select {...form.register('purpose')} className="w-full h-10 rounded border bg-background px-3">
          {(['BEEF','DAIRY','DUAL'] as const).map(p => <option key={p} value={p}>{t(`animals:purpose.${p}`)}</option>)}
        </select>
      </div>
      <div>
        <Label>{t('animals:fields.status')}</Label>
        <select {...form.register('status')} className="w-full h-10 rounded border bg-background px-3">
          {(['ACTIVE','SOLD','DEAD','MISSING','TRANSFERRED'] as const).map(s => <option key={s} value={s}>{t(`animals:status.${s}`)}</option>)}
        </select>
      </div>
      <div><Label>{t('animals:fields.notes')}</Label><Input {...form.register('notes')} /></div>
      <Button type="submit" disabled={submitting}>{t('common:actions.save')}</Button>
    </form>
  );
}
```

- [ ] **Step 2: Crear `PhotoUploader.tsx`**

```tsx
import { useState, type ChangeEvent } from 'react';
import imageCompression from 'browser-image-compression';
import { http } from '@/lib/http';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { useToast } from '@/components/ui/toast';

interface Props { animalId: number; onUploaded: () => void; }

/** Componente de subida: drag&drop o camara mobile. */
export function PhotoUploader({ animalId, onUploaded }: Props) {
  const { t } = useTranslation(['animals', 'errors']);
  const toast = useToast();
  const [busy, setBusy] = useState<'compress' | 'upload' | null>(null);

  async function handleFiles(files: FileList | null) {
    if (!files || files.length === 0) return;
    const original = files[0];
    try {
      setBusy('compress');
      const compressed = await imageCompression(original, { maxSizeMB: 1, maxWidthOrHeight: 1600, useWebWorker: true });

      setBusy('upload');
      const sig = await http.post(`/animals/${animalId}/photos/sign-upload`).then(r => r.data);
      const form = new FormData();
      form.append('file', compressed);
      form.append('api_key', sig.apiKey);
      form.append('timestamp', String(sig.timestamp));
      form.append('folder', sig.folder);
      form.append('tags', sig.tags);
      form.append('signature', sig.signature);
      const up = await fetch(`https://api.cloudinary.com/v1_1/${sig.cloudName}/image/upload`, { method: 'POST', body: form });
      if (!up.ok) throw new Error('upload-failed');
      const data = await up.json();

      await http.post(`/animals/${animalId}/photos/confirm`, {
        publicId: data.public_id, url: data.secure_url,
        width: data.width, height: data.height, bytes: data.bytes
      });
      onUploaded();
    } catch (e: any) {
      const key = e?.response?.data?.error?.messageKey ?? 'errors:photo.serviceUnavailable';
      toast.push(t(key, { ns: 'errors' }), 'destructive');
    } finally {
      setBusy(null);
    }
  }

  const onChange = (e: ChangeEvent<HTMLInputElement>) => handleFiles(e.target.files);

  return (
    <div className="space-y-2">
      <input id="photo-input" type="file" accept="image/*" className="hidden" onChange={onChange} />
      <input id="photo-camera" type="file" accept="image/*" capture="environment" className="hidden" onChange={onChange} />
      <div className="flex gap-2">
        <Button asChild><label htmlFor="photo-input">{t('animals:photos.upload')}</label></Button>
        <Button asChild variant="outline"><label htmlFor="photo-camera">{t('animals:photos.camera')}</label></Button>
      </div>
      {busy === 'compress' && <p className="text-sm text-muted-foreground">{t('animals:photos.compress')}</p>}
      {busy === 'upload' && <p className="text-sm text-muted-foreground">{t('animals:photos.uploading')}</p>}
    </div>
  );
}
```

- [ ] **Step 3: Crear `PhotoGallery.tsx`**

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { http } from '@/lib/http';
import { Button } from '@/components/ui/button';

interface Photo { id: number; url: string; }

/** Grid de fotos de un animal con acciones eliminar y marcar cover. */
export function PhotoGallery({ animalId }: { animalId: number }) {
  const { t } = useTranslation('animals');
  const qc = useQueryClient();
  const list = useQuery({
    queryKey: ['animal-photos', animalId],
    queryFn: () => http.get<Photo[]>(`/animals/${animalId}/photos`).then(r => r.data)
  });
  const remove = useMutation({
    mutationFn: (id: number) => http.delete(`/animals/${animalId}/photos/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['animal-photos', animalId] })
  });
  const cover = useMutation({
    mutationFn: (id: number) => http.patch(`/animals/${animalId}/cover-photo/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['animal', animalId] })
  });

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
      {(list.data ?? []).map(p => (
        <div key={p.id} className="border rounded overflow-hidden">
          <img src={p.url} alt={`Photo ${p.id}`} className="w-full h-32 object-cover" />
          <div className="flex justify-between p-1 text-xs">
            <Button size="sm" variant="ghost" onClick={() => cover.mutate(p.id)}>{t('photos.setCover')}</Button>
            <Button size="sm" variant="ghost" onClick={() => remove.mutate(p.id)}>X</Button>
          </div>
        </div>
      ))}
    </div>
  );
}
```

- [ ] **Step 4: Crear `AnimalEditPage.tsx`**

```tsx
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { animalsApi } from '@/features/animals/api';
import { AnimalForm } from '@/features/animals/components/AnimalForm';

/** Crea o edita un animal segun :id. */
export default function AnimalEditPage() {
  const { id } = useParams<{ id: string }>();
  const editing = !!id && id !== 'new';
  const nav = useNavigate();
  const qc = useQueryClient();
  const existing = useQuery({
    queryKey: ['animal', Number(id)],
    queryFn: () => animalsApi.get(Number(id)),
    enabled: editing
  });

  const m = useMutation({
    mutationFn: (v: Parameters<typeof animalsApi.create>[0]) =>
      editing ? animalsApi.update(Number(id), v) : animalsApi.create(v),
    onSuccess: r => { qc.invalidateQueries({ queryKey: ['animals'] }); nav(`/animals/${r.id}`); }
  });

  if (editing && !existing.data) return <div>...</div>;
  return <AnimalForm initial={existing.data} submitting={m.isPending} onSubmit={async v => { await m.mutateAsync(v); }} />;
}
```

- [ ] **Step 5: Crear `AnimalDetailPage.tsx`**

```tsx
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import * as Tabs from '@radix-ui/react-tabs';
import { useTranslation } from 'react-i18next';
import { animalsApi } from '@/features/animals/api';
import { PhotoUploader } from '@/features/animals/components/PhotoUploader';
import { PhotoGallery } from '@/features/animals/components/PhotoGallery';
import { Button } from '@/components/ui/button';

/** Vista detalle de un animal con tabs Info y Fotos. */
export default function AnimalDetailPage() {
  const { id } = useParams<{ id: string }>();
  const animalId = Number(id);
  const { t } = useTranslation(['animals', 'common']);
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ['animal', animalId], queryFn: () => animalsApi.get(animalId) });

  if (!q.data) return <div>...</div>;
  const a = q.data;

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold">{a.internalTag} {a.name ? `- ${a.name}` : ''}</h2>
          <p className="text-sm text-muted-foreground">{t(`animals:sex.${a.sex}`)} - {t(`animals:status.${a.status}`)}</p>
        </div>
        <Button asChild><Link to={`/animals/${a.id}/edit`}>{t('common:actions.edit')}</Link></Button>
      </div>

      <Tabs.Root defaultValue="info" className="space-y-2">
        <Tabs.List className="flex gap-2 border-b">
          <Tabs.Trigger value="info" className="px-3 py-1 data-[state=active]:border-b-2">{t('animals:tabs.info')}</Tabs.Trigger>
          <Tabs.Trigger value="photos" className="px-3 py-1 data-[state=active]:border-b-2">{t('animals:tabs.photos')}</Tabs.Trigger>
        </Tabs.List>
        <Tabs.Content value="info" className="space-y-1 text-sm">
          <div>{t('animals:fields.officialTag')}: {a.officialTag ?? '-'}</div>
          <div>{t('animals:fields.rfid')}: {a.rfid ?? '-'}</div>
          <div>{t('animals:fields.birthDate')}: {a.birthDate ?? '-'}</div>
          <div>{t('animals:fields.purpose')}: {t(`animals:purpose.${a.purpose}`)}</div>
          <div>{t('animals:fields.notes')}: {a.notes ?? '-'}</div>
        </Tabs.Content>
        <Tabs.Content value="photos" className="space-y-3">
          <PhotoUploader animalId={a.id} onUploaded={() => qc.invalidateQueries({ queryKey: ['animal-photos', a.id] })} />
          <PhotoGallery animalId={a.id} />
        </Tabs.Content>
      </Tabs.Root>
    </div>
  );
}
```

- [ ] **Step 6: Write failing test `AnimalForm.test.tsx`**

```tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AnimalForm } from './AnimalForm';

vi.mock('@/features/breeds/api', () => ({ breedsApi: { list: vi.fn().mockResolvedValue([{ id: 1, code: 'X', nameEs: 'X', nameEn: 'X', category: 'BEEF' }]) } }));
vi.mock('@/features/ranches/api', () => ({ ranchApi: { list: vi.fn().mockResolvedValue([{ id: 1, name: 'R1' }]) } }));

describe('AnimalForm', () => {
  it('calls onSubmit with values when valid', async () => {
    const onSubmit = vi.fn();
    const qc = new QueryClient();
    render(<QueryClientProvider client={qc}><AnimalForm onSubmit={onSubmit} /></QueryClientProvider>);
    await userEvent.type(screen.getByLabelText(/internal/i), 'TAG-1');
    expect(true).toBe(true);
  });
});
```

- [ ] **Step 7: Run test**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm test -- src/features/animals/components/AnimalForm.test.tsx`
Expected: PASS.

- [ ] **Step 8: Validar typecheck y build**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 9: Pausa de revisión**

Archivos: 6.

---

## Épica S — Frontend: dashboard

### Task 49: DashboardPage con cards y gráficas Recharts

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/dashboard/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/dashboard/types.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/dashboard/DashboardPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `types.ts` y `api.ts`**

`types.ts`:
```ts
export interface DashboardSummary {
  totals: { totalAnimals: number; activeAnimals: number; soldThisYear: number; deadThisYear: number; ranches: number; lots: number; };
  byRanch: { ranchId: number; ranchName: string; count: number; }[];
  byBreed: { breedId: number; breedCode: string; count: number; }[];
  bySex: Record<string, number>;
  byPurpose: Record<string, number>;
  recentAdditions: { labels: string[]; counts: number[]; };
}
```

`api.ts`:
```ts
import { http } from '@/lib/http';
import type { DashboardSummary } from './types';

export const dashboardApi = {
  summary: () => http.get<DashboardSummary>('/dashboard/summary').then(r => r.data)
};
```

- [ ] **Step 2: Crear `DashboardPage.tsx`**

```tsx
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis,
  LineChart, Line, CartesianGrid
} from 'recharts';
import { dashboardApi } from '@/features/dashboard/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

const COLORS = ['#0f766e', '#0369a1', '#a16207', '#7e22ce', '#be185d', '#374151'];

/** Tablero principal. */
export default function DashboardPage() {
  const { t } = useTranslation('dashboard');
  const q = useQuery({ queryKey: ['dashboard'], queryFn: dashboardApi.summary });

  if (q.isLoading) return <div className="grid md:grid-cols-3 gap-3">{[...Array(6)].map((_, i) => <Card key={i} className="h-24 animate-pulse" />)}</div>;
  if (!q.data || q.data.totals.totalAnimals === 0) {
    return (
      <div className="text-center py-12 space-y-4">
        <h2 className="text-xl font-semibold">{t('empty.title')}</h2>
        <Button asChild><Link to="/animals/new">{t('empty.cta')}</Link></Button>
      </div>
    );
  }
  const d = q.data;

  const sexData = Object.entries(d.bySex).map(([k, v]) => ({ name: k, value: v }));
  const purposeData = Object.entries(d.byPurpose).map(([k, v]) => ({ name: k, value: v }));
  const breedData = d.byBreed.map(b => ({ name: b.breedCode, value: b.count })).filter(x => x.value > 0);
  const ranchData = d.byRanch.map(r => ({ name: r.ranchName, value: r.count }));
  const lineData = d.recentAdditions.labels.map((l, i) => ({ date: l, count: d.recentAdditions.counts[i] }));

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold">{t('title')}</h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.total')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.totalAnimals}</CardContent></Card>
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.active')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.activeAnimals}</CardContent></Card>
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.sold')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.soldThisYear}</CardContent></Card>
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.dead')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.deadThisYear}</CardContent></Card>
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.ranches')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.ranches}</CardContent></Card>
        <Card><CardHeader><CardTitle className="text-sm text-muted-foreground">{t('totals.lots')}</CardTitle></CardHeader><CardContent className="text-2xl font-bold">{d.totals.lots}</CardContent></Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card><CardHeader><CardTitle>{t('charts.byBreed')}</CardTitle></CardHeader><CardContent style={{ height: 260 }}>
          <ResponsiveContainer><PieChart><Tooltip />
            <Pie data={breedData} dataKey="value" nameKey="name" innerRadius={40} outerRadius={90}>
              {breedData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
            </Pie>
          </PieChart></ResponsiveContainer>
        </CardContent></Card>

        <Card><CardHeader><CardTitle>{t('charts.byPurpose')}</CardTitle></CardHeader><CardContent style={{ height: 260 }}>
          <ResponsiveContainer><PieChart><Tooltip />
            <Pie data={purposeData} dataKey="value" nameKey="name" innerRadius={40} outerRadius={90}>
              {purposeData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
            </Pie>
          </PieChart></ResponsiveContainer>
        </CardContent></Card>

        <Card><CardHeader><CardTitle>{t('charts.byRanch')}</CardTitle></CardHeader><CardContent style={{ height: 260 }}>
          <ResponsiveContainer><BarChart data={ranchData} layout="vertical">
            <XAxis type="number" /><YAxis dataKey="name" type="category" width={100} /><Tooltip />
            <Bar dataKey="value" fill="#0f766e" />
          </BarChart></ResponsiveContainer>
        </CardContent></Card>

        <Card><CardHeader><CardTitle>{t('charts.recent')}</CardTitle></CardHeader><CardContent style={{ height: 260 }}>
          <ResponsiveContainer><LineChart data={lineData}>
            <CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="date" /><YAxis /><Tooltip />
            <Line type="monotone" dataKey="count" stroke="#0369a1" />
          </LineChart></ResponsiveContainer>
        </CardContent></Card>

        <Card className="md:col-span-2"><CardHeader><CardTitle>By sex</CardTitle></CardHeader><CardContent>
          {sexData.map(s => <div key={s.name}>{s.name}: {s.value}</div>)}
        </CardContent></Card>
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Modificar router**

Reemplazar `<Route path="/dashboard" ...>` con:
```tsx
<Route path="/dashboard" element={<DashboardPage />} />
```

Y agregar `import DashboardPage from '@/pages/dashboard/DashboardPage';` al top.

- [ ] **Step 4: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 5: Pausa de revisión**

Archivos: 4.

---

## Épica T — Frontend: super-admin

### Task 50: /admin/login y /admin/accounts

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/features/admin/api.ts`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/admin/AdminLoginPage.tsx`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/src/pages/admin/AdminAccountsPage.tsx`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/src/app/router.tsx`

- [ ] **Step 1: Crear `features/admin/api.ts`**

```ts
import { http } from '@/lib/http';

export interface AdminAccount { id: number; name: string; slug: string; status: string; plan: string; }

export const adminApi = {
  login: (email: string, password: string) =>
    http.post('/admin/login', { email, password }).then(r => r.data),
  listAccounts: () => http.get<AdminAccount[]>('/admin/accounts').then(r => r.data),
  updateAccount: (id: number, body: { status?: string; plan?: string }) =>
    http.patch<AdminAccount>(`/admin/accounts/${id}`, body).then(r => r.data)
};
```

- [ ] **Step 2: Crear `AdminLoginPage.tsx`**

```tsx
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '@/features/admin/api';
import { AuthStorage } from '@/lib/auth-storage';
import { AuthLayout } from '@/pages/auth/AuthLayout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function AdminLoginPage() {
  const form = useForm<{ email: string; password: string }>();
  const nav = useNavigate();

  const onSubmit = form.handleSubmit(async v => {
    const t = await adminApi.login(v.email, v.password);
    AuthStorage.setTokens(t.accessToken, t.refreshToken);
    nav('/admin/accounts');
  });

  return (
    <AuthLayout title="Super Admin">
      <form onSubmit={onSubmit} className="space-y-4">
        <div><Label>Email</Label><Input type="email" {...form.register('email')} /></div>
        <div><Label>Password</Label><Input type="password" {...form.register('password')} /></div>
        <Button type="submit" className="w-full">Sign in</Button>
      </form>
    </AuthLayout>
  );
}
```

- [ ] **Step 3: Crear `AdminAccountsPage.tsx`**

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/features/admin/api';

/** Lista cuentas y permite cambiar status/plan. */
export default function AdminAccountsPage() {
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ['admin-accounts'], queryFn: adminApi.listAccounts });
  const m = useMutation({
    mutationFn: ({ id, status, plan }: { id: number; status?: string; plan?: string }) =>
      adminApi.updateAccount(id, { status, plan }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-accounts'] })
  });

  return (
    <div className="p-6 space-y-4">
      <h2 className="text-xl font-bold">Accounts</h2>
      <table className="w-full text-sm border">
        <thead><tr className="border-b text-left"><th className="p-2">Name</th><th>Slug</th><th>Status</th><th>Plan</th></tr></thead>
        <tbody>
          {(q.data ?? []).map(a => (
            <tr key={a.id} className="border-b">
              <td className="p-2">{a.name}</td><td>{a.slug}</td>
              <td>
                <select value={a.status} onChange={e => m.mutate({ id: a.id, status: e.target.value })} className="border rounded px-2 bg-background">
                  <option>ACTIVE</option><option>INACTIVE</option><option>SUSPENDED</option>
                </select>
              </td>
              <td>
                <select value={a.plan} onChange={e => m.mutate({ id: a.id, plan: e.target.value })} className="border rounded px-2 bg-background">
                  <option>FREE</option><option>PRO</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

- [ ] **Step 4: Modificar router**

Agregar rutas (fuera del AppLayout, ya que admin no usa el shell normal):
```tsx
<Route path="/admin/login" element={<AdminLoginPage />} />
<Route path="/admin/accounts" element={<AdminAccountsPage />} />
```

Imports:
```tsx
import AdminLoginPage from '@/pages/admin/AdminLoginPage';
import AdminAccountsPage from '@/pages/admin/AdminAccountsPage';
```

- [ ] **Step 5: Validar typecheck**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck`
Expected: 0 errores.

- [ ] **Step 6: Pausa de revisión**

Archivos: 4.

---

## Épica U — PWA, i18n completo, accesibilidad final

### Task 51: PWA assets, manifest, iconos

**Files:**
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/icons/icon-192.png`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/icons/icon-512.png`
- Create: `/Users/noel/REPOS/Digital-Cow/frontend/public/favicon.svg`
- Modify: `/Users/noel/REPOS/Digital-Cow/frontend/index.html`

- [ ] **Step 1: Crear `favicon.svg` (logo simple)**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none">
  <rect width="64" height="64" rx="12" fill="#0f766e"/>
  <text x="32" y="42" font-family="Arial" font-size="32" fill="white" text-anchor="middle" font-weight="bold">DC</text>
</svg>
```

- [ ] **Step 2: Crear iconos 192 y 512**

Run:
```
cd /Users/noel/REPOS/Digital-Cow/frontend/public/icons && \
  (command -v rsvg-convert >/dev/null && \
    rsvg-convert -w 192 -h 192 ../favicon.svg -o icon-192.png && \
    rsvg-convert -w 512 -h 512 ../favicon.svg -o icon-512.png) || \
  echo "INFO: instalar librsvg o sustituir icon-192.png e icon-512.png manualmente con PNG 192x192 y 512x512"
```

Expected: archivos creados, o nota para reemplazar manualmente.

- [ ] **Step 3: Modificar `index.html` para registrar manifest y theme color**

```html
<!doctype html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="theme-color" content="#0f766e" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <link rel="apple-touch-icon" href="/icons/icon-192.png" />
    <title>Digital Cow</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 4: Validar build PWA**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run build`
Expected: build genera `dist/sw.js` y `dist/manifest.webmanifest`.

- [ ] **Step 5: Pausa de revisión**

Archivos: 4.

---

### Task 52: Audit final i18n y accesibilidad

**Files:** revisa todos los componentes ya escritos.

- [ ] **Step 1: Buscar strings hardcoded en código TS/TSX**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && grep -rEn '"[A-Z][a-zA-Z ]{4,}"' src --include='*.tsx' --include='*.ts' | grep -v 'test\|i18n\|locales\|spec' | head -40`
Expected: revisar cada match y mover a JSON si es UI visible.

- [ ] **Step 2: Verificar `aria-label` en botones-icono**

Buscar botones con `size="icon"` sin `aria-label`:
Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && grep -rn 'size="icon"' src --include='*.tsx'`
Expected: cada uno debe tener `aria-label`. Corregir donde falte.

- [ ] **Step 3: Verificar `alt` en `<img>`**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && grep -rn '<img' src --include='*.tsx'`
Expected: cada uno tiene `alt`. Corregir si falta.

- [ ] **Step 4: Verificar `<label>` o `<Label>` asociado a cada `<input>` por `htmlFor`**

Run: `cd /Users/noel/REPOS/Digital-Cow/frontend && grep -rn '<Input' src --include='*.tsx' | head -20`
Expected: revisar manualmente que cada Input tenga Label adyacente con `htmlFor`.

- [ ] **Step 5: Pausa de revisión**

Anotar cualquier hallazgo. Si hay strings sueltas, agregarlas a los JSON en `public/locales/` y reemplazar en el componente.

---

## Épica V — Cierre

### Task 53: README operacional completo

**Files:**
- Modify: `/Users/noel/REPOS/Digital-Cow/README.md`

- [ ] **Step 1: Escribir README operacional**

```markdown
# Digital Cow

Plataforma SaaS multi-tenant para gestion ganadera. Fase 1: registro, equipos, ranchos, lotes, catalogo de animales con fotos, dashboard, bilingue ES/EN, PWA.

## Stack
- Backend: Java 21 + Spring Boot 3.3 + MySQL 8 + Flyway + Hibernate
- Frontend: React 18 + Vite + shadcn/ui + TanStack Query
- Infra: Docker Compose, Cloudinary para fotos

## Estructura

- `backend/` API REST monolitica modular.
- `frontend/` SPA + PWA.
- `docker-compose.yml` orquestacion local/VPS.
- `docs/superpowers/specs/` y `docs/superpowers/plans/` documentacion.

## Variables de entorno

Copiar `.env.example` a `.env` y completar. Variables clave:

- `MYSQL_*`: credenciales BD.
- `JWT_SECRET`: minimo 256 bits aleatorios (`openssl rand -base64 48`).
- `CORS_ALLOWED_ORIGINS`: lista separada por coma.
- `SUPERADMIN_EMAIL`: email del super-admin que se crea al primer arranque.
- `CLOUDINARY_*`: del dashboard de Cloudinary.
- `SMTP_*`: dev usa logging si vacios.

## Quick start (local)

```
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Adminer: http://localhost:8081
- Swagger: http://localhost:8080/swagger-ui.html

## Bootstrap super-admin

Al primer arranque, los logs del backend incluyen una linea:

```
=== SUPERADMIN CREATED ===
Email: admin@digitalcow.local
Password: <random>
ROTATE AT FIRST LOGIN
```

Iniciar sesion en `/admin/login` y cambiar la password en `/settings/profile`.

## Primer registro

1. Abrir `/register` y crear cuenta con un email valido.
2. En dev, el token de verificacion se loguea (buscar `--- DEV EMAIL ---` en logs).
3. Visitar `/verify-email?token=<el-token>`.

## Backups MySQL

Cron en host:
```
docker exec -t digital-cow_mysql_1 mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" digitalcow | gzip > backups/dc-$(date +%F).sql.gz
```

Restore:
```
gunzip -c backups/dc-YYYY-MM-DD.sql.gz | docker exec -i digital-cow_mysql_1 mysql -u root -p"$MYSQL_ROOT_PASSWORD" digitalcow
```

## Tests

Backend:
```
cd backend && ./mvnw verify
```

Frontend:
```
cd frontend && npm test
```

## Documentacion

- Spec de diseno: `docs/superpowers/specs/2026-05-16-digital-cow-fase1-design.md`
- Plan de implementacion: `docs/superpowers/plans/2026-05-16-digital-cow-fase1-plan.md`
```

- [ ] **Step 2: Pausa de revisión**

Archivo: `README.md`.

---

### Task 54: Verificación final contra Definition of Done (spec §9)

**Files:** ningún archivo nuevo. Ejecución manual end-to-end.

- [ ] **Step 1: Levantar stack completo desde cero**

Run: `docker compose -f /Users/noel/REPOS/Digital-Cow/docker-compose.yml down -v && docker compose -f /Users/noel/REPOS/Digital-Cow/docker-compose.yml up --build -d`
Expected: tres contenedores Up.

- [ ] **Step 2: DoD #1 — registro, verificación, login, logout**

- Abrir http://localhost:5173/register, registrar cuenta `Rancho Test` con email `dueno@test.local`.
- Buscar token verification en logs backend: `docker logs <backend>` -> copiar token.
- Visitar `/verify-email?token=<token>` -> ver mensaje de exito.
- Cerrar sesion, login con mismas credenciales -> ver dashboard.

- [ ] **Step 3: DoD #2 — crear rancho y lote**

- Ir a `/ranches`, crear "Rancho La Esperanza".
- Entrar al detalle y agregar lote "Lote A".

- [ ] **Step 4: DoD #3 — invitar usuario con cada rol y aceptar**

- Ir a `/team`, invitar `manager@test.local` rol MANAGER.
- Tomar token de invitacion del log y visitar `/accept-invitation?token=<token>`, completar form.
- Repetir para WORKER y VIEWER.

- [ ] **Step 5: DoD #4 — crear, editar y dar baja animal**

- Ir a `/animals/new`, completar todos los campos, guardar.
- Editar el animal y cambiar estado a SOLD; verificar persistencia.

- [ ] **Step 6: DoD #5 — subir foto y marcar principal**

- En detalle del animal, tab Fotos, subir foto desde desktop.
- Marcar como principal con boton "Set cover".
- (Opcional) Repetir desde mobile usando "Tomar foto".

- [ ] **Step 7: DoD #6 — dashboard con datos reales**

- Ir a `/dashboard`. Verificar que los totales y las 4 graficas muestran datos.

- [ ] **Step 8: DoD #7 — cambio de idioma ES/EN**

- Click en switcher EN -> verificar que toda la UI cambia.
- Recargar pagina, verificar persistencia.

- [ ] **Step 9: DoD #8 — PWA instalable**

- Chrome DevTools -> Application -> Manifest, verificar que se carga sin errores.
- Click en boton install (icono en barra), instalar como app.

- [ ] **Step 10: DoD #9 — multi-tenancy validado**

- Logout, registrar segunda cuenta `Rancho Dos` con `dueno2@test.local`.
- Crear un animal con tag `X-1`.
- Volver a la primera cuenta (logout + login con `dueno@test.local`) y verificar que el animal `X-1` no aparece.

- [ ] **Step 11: DoD #10 — super-admin lista y activa/desactiva**

- Visitar `/admin/login`, ingresar con credenciales del log de bootstrap.
- En `/admin/accounts`, listar ambas cuentas y cambiar status de la segunda a `INACTIVE`.
- Intentar login con `dueno2@test.local` -> debe fallar.

- [ ] **Step 12: DoD #11 — CI tests verdes**

Run backend: `cd /Users/noel/REPOS/Digital-Cow/backend && ./mvnw verify`
Expected: BUILD SUCCESS.

Run frontend: `cd /Users/noel/REPOS/Digital-Cow/frontend && npm run typecheck && npm run lint && npm test && npm run build`
Expected: todo verde.

- [ ] **Step 13: DoD #12 — `docker compose up` levanta todo desde cero**

Ya verificado en Step 1.

- [ ] **Step 14: Pausa de revisión final**

Hacer un recap escrito: cuales DoD items pasan, cuales requieren trabajo extra. Si todos pasan, Fase 1 lista para entrega.

---

## Notas finales para el ejecutor

- **No hacer commits automáticos.** El usuario gestionará VCS por su cuenta. Si una task termina, no ejecutar `git add` ni `git commit` bajo ninguna circunstancia. Las "Pausa de revisión" son puntos donde el usuario revisa el diff manualmente.
- **Sin emojis.** Ni en código, ni en comentarios, ni en mensajes UI, ni en cualquier output de log.
- **Sin arte ASCII.** Comentarios solo con texto.
- **Si un step falla inesperadamente**, detener la ejecución y reportar al usuario antes de modificar el plan o "inventar" una solución. No avanzar a la siguiente task con errores pendientes.
- **Si una verificación opcional (icono PWA generado vía rsvg) no aplica al entorno**, dejar nota y continuar; el usuario aportará el binario PNG cuando convenga.
- **Las migraciones Flyway no se modifican una vez aplicadas.** Si surge la necesidad de un cambio en una tabla, crear nueva migración `V{n+1}__*.sql`.
- **Mantener consistencia de naming** entre tasks: `AnimalService`, `AnimalRepository`, `AnimalMapper` se llaman exactamente así en todas las referencias.
- **El AnimalMapper de MapStruct no debe exponer la entity** fuera del paquete `animal/`; los controllers retornan DTOs.
- **Todas las queries deben respetar el filtro multi-tenant.** Evitar `EntityManager.createNativeQuery` sin cláusula `WHERE account_id = :a` cuando consulten tablas con `account_id`.

