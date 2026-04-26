# ==========================================
# ÉTAPE 1 : BUILD (Compilation du projet)
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# ÉTAPE 2 : RUN (Exécution de l'application)
# ==========================================
FROM eclipse-temurin:25-jre-alpine

# 👉 CORRECTION : On met le .jar à l'abri dans le dossier /opt
COPY --from=builder /app/target/*.jar /opt/app.jar

# On définit /app comme dossier de travail pour la base de données H2
WORKDIR /app

EXPOSE 8080

# On lance le .jar depuis sa nouvelle cachette
ENTRYPOINT ["java", "-jar", "/opt/app.jar"]