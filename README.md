# Pollair_API
 
## Initialisation de la base de données

L'API de pollair est configuré sur un serveur PostGreSQL. Avant toute configuration de l'api, la base de données doit être initialisée :

1 - Créer une nouvelle base de données nommé pollair ( Si un autre nom est défini, voir configuration à apporter dans la section [Configuration de l'API](#configuration-de-l'api)
2 - Lancer le script initbdd.sql contenu dans le dossier DatabaseFiles

## Configuration de l'API

Pollair_API est produite sur Spring Boot

Avant toute execution : 
1 - Configurer le lien à votre base dans le fichier application.properties : 
- spring.datasource.url : doit correspondre au port et au nom de votre bdd : jdbc:postgresql://localhost:{port}/{nom bdd}
- spring.datasource.username : correspond à l'identifiant de l'user pour la connexion à votre base. L'user par défaut de PgAdmin est initialisé dans le dépôt
- spring.datasource.password : correspond au mot de passe de l'user pour la connexion à votre base. L'user par défaut de PgAdmin est initialisé dans le dépôt


## Lancement de l'API

Le projet Spring Boot utilise maven pour build et s'exécuter. un script Maven Wrapper est inclus dans le projet pour l'exécution.

À partir de votre terminal préféré, lancer la commande : 
```bash
./mvnw spring-boot:run
```
