# 🎸 Serveur Back-End Python (Django)

[⬅️ Retour à la racine du projet](../../README.md)

## 📑 Sommaire
- [🎸 Serveur Back-End Python (Django)](#-serveur-back-end-python-django)
  - [📑 Sommaire](#-sommaire)
  - [📖 Présentation du module](#-présentation-du-module)
  - [🏗️ Architecture et Fichiers Clés](#️-architecture-et-fichiers-clés)
    - [⚙️ Configuration \& Infrastructure](#️-configuration--infrastructure)
    - [💾 Modèles \& Logique (Couche Data)](#-modèles--logique-couche-data)
    - [🧠 Services \& Moteur de Jeu](#-services--moteur-de-jeu)
    - [🔌 API \& Sérialisation (Couche Web)](#-api--sérialisation-couche-web)
    - [🛡️ Sécurité \& Authentification](#️-sécurité--authentification)
    - [🛠️ Scripts de Gestion](#️-scripts-de-gestion)
  - [⚙️ Installation et Configuration](#️-installation-et-configuration)
    - [🔑 Configuration du fichier .env (Obligatoire)](#-configuration-du-fichier-env-obligatoire)
  - [🚀 Comment lancer le serveur ?](#-comment-lancer-le-serveur-)
    - [🐋 Méthode A : Docker (Automatisé)](#-méthode-a--docker-automatisé)
    - [💻 Méthode B : Lancement Manuel (Développement)](#-méthode-b--lancement-manuel-développement)
  - [🛑 Comment arrêter le serveur ?](#-comment-arrêter-le-serveur-)

---

## 📖 Présentation du module

Ce module constitue le second pilier back-end du projet. Développé avec **Python 3.12** et **Django**, il offre une alternative performante au serveur Java. Sa mission principale est de servir de cerveau pour les adversaires automatisés (Bots) et d'assurer la gestion de la banque de questions via un système d'importation SQL optimisé.

**Points clés :**
* **Intelligence des Bots :** Gère les décisions et les réponses des joueurs IA.
* **Moteur de Jeu Redondant :** Implémentation complète des règles permettant de jouer indépendamment de Spring.
* **Importation SQL :** Transforme instantanément le fichier `questions_bdd.sql` en données structurées.
* **Authentification Hybride :** Supporte les comptes locaux et l'OAuth2 (Google/GitHub).

---

## 🏗️ Architecture et Fichiers Clés

### ⚙️ Configuration & Infrastructure
* **`settings.py`** : Configuration centrale (Base SQLite, middleware CORS, gestion des variables d'environnement).
* **`requirements.txt`** : Dépendances Python (Django, Django REST Framework, PyJWT, Python-dotenv, Faker).
* **`Dockerfile`** : Image basée sur `python:3.12-slim` pour un déploiement léger.
* **`entrypoint.sh`** : Script d'automatisation qui lance les migrations, importe les questions et crée les comptes de démo au démarrage du conteneur.

### 💾 Modèles & Logique (Couche Data)
* **`models.py`** : Schéma de la base (Utilisateurs, Questions, Parties, Joueurs, Historique, Grille).
* **`managers.py`** : Logique de sélection intelligente (ex: tirage de questions inédites).

### 🧠 Services & Moteur de Jeu
* **`services.py`** : Logique de mouvement, gestion des dés et déclenchement des effets de cases.
* **`ia_juge_service.py`** : Algorithme de distance de Levenshtein pour la tolérance orthographique.

### 🔌 API & Sérialisation (Couche Web)
* **`views.py`** : Contrôleurs traitant les requêtes du Front (Jeu, Authentification, OAuth2).
* **`serializers.py`** : Traduction des modèles SQL en format JSON pour Angular.
* **`urls.py`** : Définition des routes de l'API (ex: `/api/jeu/repondre/`).

### 🛡️ Sécurité & Authentification
* **`security.py`** : Service JWT et filtre d'authentification pour sécuriser les accès API.

### 🛠️ Scripts de Gestion
* **`importer_questions.py`** : Commande personnalisée pour injecter les 120 questions du fichier SQL.
* **`init_demo.py`** : Initialise les comptes `admin@demo.com` et `joueur@demo.com`.
* **`jouer_cli.py`** : Interface en ligne de commande pour tester le moteur sans interface graphique.

---

## ⚙️ Installation et Configuration

### 🔑 Configuration du fichier .env (Obligatoire)
Pour activer les fonctionnalités de connexion et la sécurité JWT, vous devez configurer vos secrets.

1.  Créez un fichier **`.env`** dans le dossier `BACKDJANGO/backdjango/`.
2.  Utilisez le modèle suivant (basé sur le fichier `env.example` inclus) :

    # --- AUTHENTIFICATION SOCIALE ---
    GITHUB_CLIENT_ID=votre_client_id_github_ici
    GITHUB_CLIENT_SECRET=votre_secret_github_ici

    GOOGLE_CLIENT_ID=votre_client_id_google_ici
    GOOGLE_CLIENT_SECRET=votre_secret_google_ici

    # --- SÉCURITÉ ---
    JWT_SECRET_KEY=mettez_une_cle_secrete_tres_longue_ici

---

## 🚀 Comment lancer le serveur ?

### 🐋 Méthode A : Docker (Automatisé)
*Méthode recommandée pour une installation "clés en main".*
1.  Placez-vous à la **racine du projet** (où se trouve le fichier `docker-compose.yml`).
2.  Exécutez la commande suivante dans votre terminal :

    sudo docker-compose up --build -d backdjango

*Le serveur sera accessible sur le port 8000 et la base sera automatiquement remplie.*

### 💻 Méthode B : Lancement Manuel (Développement)
*Prérequis : Python 3.12 installé sur votre machine.*
1.  Entrez dans le dossier du serveur :
    
    cd BACKDJANGO/backdjango/

2.  Installez les dépendances nécessaires :
    
    pip install -r requirements.txt

3.  Initialisez la base de données (uniquement la première fois) :

    python manage.py migrate
    python manage.py importer_question
    python manage.py init_demo

4.  Démarrez le serveur localement :

    python manage.py runserver 0.0.0.0:8000

---

## 🛑 Comment arrêter le serveur ?

* **Si lancé avec Docker :** Tapez la commande suivante :
  
  sudo docker-compose stop backdjango

* **Si lancé manuellement :** Faites simplement un **`Ctrl + C`** dans le terminal de commande où s'exécute le serveur.

---
[⬅️ Retour à la racine du projet](../../README.md)