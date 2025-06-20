FROM maven:3.9.6-openjdk-23-slim

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Compilar
RUN mvn clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Ejecutar
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]