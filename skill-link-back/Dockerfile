# Usar imagen base de Java 23
FROM openjdk:23-jdk-slim

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Compilar
RUN mvn clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Ejecutar
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]