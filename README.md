# 🚀 Projet Jeu de Plateau IA : Multi-Backend (Spring & Django)

## 📑 Sommaire
- [🚀 Projet Jeu de Plateau IA : Multi-Backend (Spring \& Django)](#-projet-jeu-de-plateau-ia--multi-backend-spring--django)
  - [📑 Sommaire](#-sommaire)
  - [📖 Présentation du Projet](#-présentation-du-projet)
  - [🏗️ Architecture et Composants](#️-architecture-et-composants)
  - [🛠️ Prérequis](#️-prérequis)
  - [🔑 Tutoriel : Obtenir vos Clés API (OAuth2)](#-tutoriel--obtenir-vos-clés-api-oauth2)
    - [🌐 Google Cloud Console](#-google-cloud-console)
    - [🐙 GitHub Developer Settings](#-github-developer-settings)
  - [🚀 Comment lancer le projet ?](#-comment-lancer-le-projet-)
    - [🐋 Méthode 1 : Docker Compose (Recommandé)](#-méthode-1--docker-compose-recommandé)
    - [🖥️ Méthode 2 : Lancement Manuel (Développement)](#️-méthode-2--lancement-manuel-développement)
  - [🎮 Règles du Jeu et Effets](#-règles-du-jeu-et-effets)
    - [Déroulement](#déroulement)
    - [Les Catégories](#les-catégories)
    - [Les Effets de Cases](#les-effets-de-cases)
  - [🛑 Comment arrêter le jeu ?](#-comment-arrêter-le-jeu-)

---

## 📖 Présentation du Projet

Ce projet est une application web de jeu de plateau interactive où les joueurs affrontent des Intelligences Artificielles (Bots). L'objectif est d'atteindre la case finale en répondant correctement à des questions de culture générale.

**L'innovation majeure :** L'application possède une architecture hybride. L'interface (Angular) peut basculer en un clic entre deux moteurs back-end différents :
* Un serveur **Java (Spring Boot)** pour la robustesse et la gestion des données H2.
* Un serveur **Python (Django)** pour la logique des Bots et le traitement SQL performant.

---

## 🏗️ Architecture et Composants

Le projet est divisé en trois modules indépendants. Cliquez sur les liens ci-dessous pour accéder à leur documentation technique détaillée :

* 🎨 [**FRONT (Angular)**](./FRONT/README.md) : L'interface utilisateur interactive (Port 4200).
* ☕ [**BACKSPRING (Java)**](./BACKSPRING/README.md) : Le moteur de jeu principal en Spring Boot (Port 8080).
* 🎸 [**BACKDJANGO (Python)**](./BACKDJANGO/README.md) : Le cerveau des bots et moteur alternatif (Port 8000).

---

## 🛠️ Prérequis

Pour faire tourner le projet, vous devez installer :
* **Docker** et **Docker Compose** (Méthode recommandée).
* *Ou pour le développement manuel :* Node.js v24+, Java JDK 25 et Python 3.12.

---

## 🔑 Tutoriel : Obtenir vos Clés API (OAuth2)

Pour activer la connexion via Google ou GitHub, vous devez créer vos propres identifiants.

### 🌐 Google Cloud Console
1. Allez sur [Google Cloud Console](https://console.cloud.google.com/).
2. Créez un nouveau projet.
3. Dans **API et services > Identifiants**, cliquez sur **Créer des identifiants > ID de client OAuth**.
4. Configurez l'écran de consentement (choisissez "Externe").
5. Type d'application : **Application Web**.
6. **Origines JavaScript autorisées :** `http://localhost:4200`
7. **URI de redirection autorisés :**
   - Pour Spring : `http://localhost:8080/login/oauth2/code/google`
   - Pour Django : `http://localhost:8000/api/auth/google/callback/`
8. Récupérez votre **Client ID** et **Client Secret**.

### 🐙 GitHub Developer Settings
1. Allez dans vos **Settings** GitHub > **Developer Settings** > **OAuth Apps**.
2. Cliquez sur **New OAuth App**.
3. **Homepage URL :** `http://localhost:4200`
4. **Authorization callback URL :**
   - Pour Spring : `http://localhost:8080/login/oauth2/code/github`
   - Pour Django : `http://localhost:8000/api/auth/github/callback/`
5. Enregistrez et générez un **Client Secret**.

*Note : Une fois les clés obtenues, reportez-les dans les fichiers `application-secret.properties` (Spring) et `.env` (Django) comme expliqué dans leurs README respectifs.*

---

## 🚀 Comment lancer le projet ?

### 🐋 Méthode 1 : Docker Compose (Recommandé)
Cette commande télécharge les images, compile le code et configure le réseau automatiquement.
1. Placez-vous à la racine (ici même).
2. Lancez la commande :
   
    sudo docker-compose up --build

3. Attendez que les logs stabilisent. L'interface est alors disponible sur : **http://localhost:4200**

### 🖥️ Méthode 2 : Lancement Manuel (Développement)
Vous devrez ouvrir trois terminaux différents :

**Terminal 1 : Back-End Spring**
    cd BACKSPRING/backspring/
    ./mvnw spring-boot:run

**Terminal 2 : Back-End Django**
    cd BACKDJANGO/backdjango/
    pip install -r requirements.txt
    python manage.py migrate
    python manage.py runserver

**Terminal 3 : Front-End Angular**
    cd FRONT/front/
    npm install --legacy-peer-deps
    ng serve

---

## 🎮 Règles du Jeu et Effets

### Déroulement
1. Connectez-vous et créez une partie dans le **Salon**.
2. À chaque tour, une question vous est posée.
3. **Bonne réponse :** Vous lancez le dé et avancez.
4. **Mauvaise réponse :** Vous restez sur place (ou reculez selon la case).
5. Le premier arrivé à la case 50 gagne la partie.

### Les Catégories
Histoire, Géographie, Sciences, Divertissement, Sport, Arts et Littérature.

### Les Effets de Cases
* 🛡️ **BOUCLIER :** Vous protège du prochain malus ou d'une explosion.
* 🏃 **SPRINT :** Double la valeur de votre prochain dé.
* 💣 **RISQUE EXPLOSIF :** Si vous répondez mal, tout le monde recule !
* 🔄 **CIBLE ECHANGE :** Permet d'échanger votre position avec un adversaire.
* 🎯 **CIBLE RECUL :** Force un adversaire de votre choix à reculer de 5 cases.
* ⚖️ **IA JUGE :** Vos réponses textuelles sont analysées par un algorithme qui accepte les petites fautes d'orthographe.

---

## 🛑 Comment arrêter le jeu ?

* **Si lancé avec Docker :**
  Appuyez sur `Ctrl + C` dans le terminal, puis tapez :
  
    sudo docker-compose down

* **Si lancé manuellement :**
  Appuyez sur `Ctrl + C` dans chacun des trois terminaux ouverts.

---
🚀 **Bon jeu !**