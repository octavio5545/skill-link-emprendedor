# Usar imagen base de Java 23
FROM openjdk:23-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copiar código fuente
COPY src src

# Dar permisos al wrapper de Maven
RUN chmod +x ./mvnw

# Compilar la aplicación (esto se hace EN EL SERVIDOR de Render)
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]