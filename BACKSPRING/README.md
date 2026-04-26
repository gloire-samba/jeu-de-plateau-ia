# ☕ Serveur Back-End Java (Spring Boot)

[⬅️ Retour à la racine du projet](../../README.md)

## 📑 Sommaire
- [☕ Serveur Back-End Java (Spring Boot)](#-serveur-back-end-java-spring-boot)
  - [📑 Sommaire](#-sommaire)
  - [📖 Présentation du module](#-présentation-du-module)
  - [🏗️ Architecture et Fichiers Clés](#️-architecture-et-fichiers-clés)
    - [⚙️ Configuration \& Infrastructure](#️-configuration--infrastructure)
    - [💾 Couche de Données (Modèles \& Repositories)](#-couche-de-données-modèles--repositories)
    - [🧠 Couche Logique (Services Métiers)](#-couche-logique-services-métiers)
    - [🔌 Couche API (Contrôleurs)](#-couche-api-contrôleurs)
    - [🛡️ Sécurité \& Authentification](#️-sécurité--authentification)
  - [🛠️ Installation et Configuration](#️-installation-et-configuration)
    - [🔑 Gestion des Secrets (Obligatoire)](#-gestion-des-secrets-obligatoire)
  - [🚀 Comment lancer le serveur ?](#-comment-lancer-le-serveur-)
    - [🖥️ Méthode A : Interface Graphique (IDE)](#️-méthode-a--interface-graphique-ide)
    - [💻 Méthode B : Ligne de commande (Maven)](#-méthode-b--ligne-de-commande-maven)
    - [🐋 Méthode C : Docker (Conteneurisé)](#-méthode-c--docker-conteneurisé)
  - [🛑 Comment arrêter le serveur ?](#-comment-arrêter-le-serveur-)

---

## 📖 Présentation du module

Ce module constitue le cœur décisionnel du projet. Développé avec **Spring Boot 4.0.5** et **Java 25 (Eclipse Temurin)**, il centralise les règles métier, la gestion du plateau et l'arbitrage des réponses. Il assure une communication fluide avec le serveur Python pour l'intelligence des bots tout en garantissant la sécurité des données via JWT.

**Points forts :**
* **Moteur de Jeu complet :** Gestion des lancers de dés, des positions et des 12 effets de cases.
* **Arbitrage Linguistique :** Utilisation de la distance de Levenshtein pour tolérer les erreurs de frappe.
* **Sécurité Multi-Source :** Authentification classique et OAuth2 (Google/GitHub) intégrée.
* **Données Persistantes :** Base H2 configurée en mode fichier (`./jeu_plateau_db`) pour ne rien perdre après redémarrage.

---

## 🏗️ Architecture et Fichiers Clés

L'application suit un découpage strict en couches pour garantir une modularité totale.

### ⚙️ Configuration & Infrastructure
* **`pom.xml`** : Gère l'écosystème du projet (Spring Data JPA, Security, OAuth2, JJWT, DataFaker).
* **`Dockerfile`** : Optimisé en deux étapes (Build avec JDK 25 et Run avec JRE 25 alpine) pour un conteneur léger et sécurisé.
* **`application.properties`** : Configuration centrale incluant le profil `secret`, les paramètres H2 et l'URL de l'IA distante.

### 💾 Couche de Données (Modèles & Repositories)
* **`entity/`** : Structure SQL (`Utilisateur`, `Partie`, `Question`, `PartieJoueur`, `CasePlateauEntity`, `HistoriqueQuestion`).
* **`repository/`** : Interfaces JPA (`PartieRepository`, `QuestionRepository`, etc.) incluant des requêtes natives `RAND()` pour le tirage des questions.
* **`dto/`** : Objets de transfert (`PartieEtatDto`, `BotJouerRequest`, `CasePlateauDto`) pour une communication propre avec Angular et Django.

### 🧠 Couche Logique (Services Métiers)
* **`MoteurJeuService`** : Logique de déplacement, gestion des tours et application des effets spéciaux (Bouclier, Sprint, Echange...).
* **`IaJugeService`** : Système d'évaluation des réponses avec nettoyage des textes (accents, articles) et tolérance proportionnelle.
* **`IAService`** : Client HTTP communiquant avec l'API Python pour piloter les bots.
* **`HistoriqueQuestionService`** : Mécanisme anti-doublon garantissant une expérience de jeu renouvelée.
* **`FakerService`** : Service d'initialisation automatique générant 10 utilisateurs et 20 parties de test au premier lancement.
* **`CasePlateauService`** : Création et gestion dynamique de la grille de jeu.

### 🔌 Couche API (Contrôleurs)
* **`JeuRestController`** : Orchestration des tours de jeu et de l'état de la partie.
* **`AuthRestController`** : Endpoints d'inscription et de login (Génération du JWT).
* **`CasePlateauController`** : API dédiée à la structure visuelle du plateau.

### 🛡️ Sécurité & Authentification
* **`SecurityConfig`** : Configuration du pare-feu applicatif, des rôles (Admin/User) et des politiques CORS.
* **`JwtFilter` & `JwtService`** : Interception et validation des jetons de session.
* **`OAuth2LoginSuccessHandler`** : Gestionnaire de succès pour Google/GitHub avec redirection vers le port 4200.

---

## 🛠️ Installation et Configuration

### 🔑 Gestion des Secrets (Obligatoire)
Pour que les fonctionnalités d'authentification et les API sociales fonctionnent, vous **devez** configurer vos clés privées.

1.  Créez le fichier suivant : `src/main/resources/application-secret.properties`.
2.  Ajoutez-y vos identifiants personnels :

    jwt.secret=VOTRE_CLE_JWT_ULTRA_SECURISEE_MIN_32_CHARS
    
    spring.security.oauth2.client.registration.google.client-id=VOTRE_ID_GOOGLE
    spring.security.oauth2.client.registration.google.client-secret=VOTRE_SECRET_GOOGLE
    
    spring.security.oauth2.client.registration.github.client-id=VOTRE_ID_GITHUB
    spring.security.oauth2.client.registration.github.client-secret=VOTRE_SECRET_GITHUB

---

## 🚀 Comment lancer le serveur ?

### 🖥️ Méthode A : Interface Graphique (IDE)
*Méthode idéale pour le développement et le debug.*
1.  Ouvrez le projet dans IntelliJ IDEA, VS Code ou Eclipse.
2.  Localisez le fichier : `src/main/java/com/iaspring/backspring/BackspringApplication.java`.
3.  Faites un clic droit sur le fichier et choisissez **"Run 'BackspringApplication'"** ou cliquez sur la flèche verte.

### 💻 Méthode B : Ligne de commande (Maven)
*Pour lancer le serveur rapidement sans IDE.*
Ouvrez un terminal dans le dossier `BACKSPRING/backspring/` et exécutez :
* **Linux / macOS :** `./mvnw spring-boot:run`
* **Windows :** `.\mvnw spring-boot:run`

### 🐋 Méthode C : Docker (Conteneurisé)
*Pour un lancement dans l'environnement de production.*
Depuis la racine du projet (là où se trouve le fichier `docker-compose.yml`) :
`sudo docker-compose up --build -d backspring`

---

## 🛑 Comment arrêter le serveur ?

* **Via l'IDE :** Cliquez sur le bouton d'arrêt (carré rouge) dans la console d'exécution.
* **Via Maven (Terminal) :** Appuyez sur `Ctrl + C` dans la fenêtre du terminal.
* **Via Docker :** Exécutez `sudo docker-compose stop backspring`.

---
[⬅️ Retour à la racine du projet](../../README.md)