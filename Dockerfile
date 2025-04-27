# Etapa 1: Build de la app
FROM gradle:8.5.0-jdk17 AS builder
 
# Copia el código fuente
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
 
# Construye el jar
RUN gradle build --no-daemon
 
# Etapa 2: Imagen final minimalista
FROM eclipse-temurin:17-jre
 
# Directorio de la app en el contenedor
WORKDIR /app
 
# Copia solo el .jar generado
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
 
# Exponer el puerto
EXPOSE 8080
 
# Comando para arrancar
ENTRYPOINT ["java", "-jar", "app.jar"]
