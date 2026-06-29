# =====================================================================
# Dockerfile di RIFERIMENTO per un'applicazione Spring Boot + Thymeleaf
# (applicazione monolitica: backend + viste in un unico JAR)
#
# Questo file mostra la versione "fatta bene" con multi-stage build.
# Per un gruppo indietro va benissimo anche la versione minima:
# vedi in fondo, sezione "ALTERNATIVA MINIMA".
# =====================================================================

# ---------- STADIO 1: BUILD ----------
# Compila il progetto e produce il "fat jar". Usa un'immagine con Maven.
# (se il gruppo usa Gradle, l'idea e' identica: cambia solo il comando)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copio prima il pom.xml e scarico le dipendenze:
# cosi' Docker mette in cache questo strato e non riscarica tutto a ogni modifica del codice.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ora copio il codice sorgente e compilo, saltando i test per velocizzare la build dell'immagine
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- STADIO 2: RUNTIME ----------
# Immagine finale leggera: solo il Java Runtime, niente Maven ne' sorgenti.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copio SOLO il jar prodotto dallo stadio di build.
# (il nome del jar dipende da artifactId/version nel pom: adattalo se serve,
#  oppure usa il pattern con jolly come qui sotto)
COPY --from=build /app/target/*.jar app.jar

# La porta su cui Spring Boot ascolta di default
EXPOSE 8080

# Comando di avvio del container
ENTRYPOINT ["java", "-jar", "app.jar"]


# =====================================================================
# ALTERNATIVA MINIMA (obiettivo realistico per un gruppo indietro)
# Se il JAR e' GIA' stato compilato a mano (mvn package) e si trova
# in target/, basta questo Dockerfile a stadio singolo:
#
#   FROM eclipse-temurin:21-jre
#   WORKDIR /app
#   COPY target/*.jar app.jar
#   EXPOSE 8080
#   ENTRYPOINT ["java", "-jar", "app.jar"]
#
# Funziona uguale: e' solo meno "pulito" perche' richiede di ricordarsi
# di compilare prima a mano. Per un primo risultato e' piu' che dignitoso.
# =====================================================================
