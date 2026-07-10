# Étape 1 : Construction (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers de configuration Maven et télécharger les dépendances
COPY pom.xml .
# Optionnel : RUN mvn dependency:go-offline pour mettre en cache les dépendances (accélère les builds suivants)

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le fichier JAR compilé depuis l'étape de construction
COPY --from=build /app/target/*.jar app.jar

# Exposer le port (Render fournira la variable d'environnement PORT, 3000 par défaut)
EXPOSE 3000

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
