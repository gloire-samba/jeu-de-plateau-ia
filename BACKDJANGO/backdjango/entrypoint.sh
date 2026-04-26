#!/bin/bash

# Arrêter le script immédiatement si une commande échoue
set -e

echo "⏳ Application des migrations de la base de données..."
python manage.py makemigrations
python manage.py migrate

echo "📥 Importation des questions..."
# On ajoute "|| true" pour ne pas faire planter le script si les questions existent déjà
python manage.py importer_question || true

echo "🎮 Initialisation des données de démo..."
python manage.py init_demo || true

echo "🚀 Démarrage du serveur Django..."
# La commande "exec $@" lance la commande CMD définie dans le Dockerfile (runserver)
exec "$@"