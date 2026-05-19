# Imagen multi-etapa que compila el backend en una etapa con JDK y lo ejecuta en otra etapa con solo JRE.
# La primera etapa descarga dependencias y compila el jar.
# La segunda etapa usa un usuario no privilegiado para correr la aplicacion en produccion.

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copia primero el wrapper de Maven y el pom para aprovechar la cache de Docker en la descarga de dependencias.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copia el codigo fuente al final para que cambios menores no invaliden la cache de dependencias.
COPY src ./src
RUN ./mvnw clean package -DskipTests -B && \
    cp target/*.jar /app/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

# Crea un usuario no root para correr la aplicacion con menos privilegios.
RUN groupadd --system app && useradd --system --gid app --home-dir /app --shell /sbin/nologin app

COPY --from=builder /app/app.jar /app/app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8080

# El healthcheck consulta el endpoint de actuator. Docker marca el contenedor como unhealthy si falla varias veces.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JAVA_OPTS permite ajustar memoria desde docker-compose. La fuente de entropia evita bloqueos al iniciar.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
