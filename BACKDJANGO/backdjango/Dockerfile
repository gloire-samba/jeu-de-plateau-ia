# ==========================================
# ENVIRONNEMENT PYTHON 3.12
# ==========================================
FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

# 👉 NOUVEAU : On donne le droit à Linux d'exécuter notre script
RUN chmod +x /app/entrypoint.sh

EXPOSE 8000

# 👉 NOUVEAU : On définit notre script comme point d'entrée officiel du conteneur
ENTRYPOINT ["/app/entrypoint.sh"]

# Commande par défaut qui sera passée à l'entrypoint (pour remplacer le "$@")
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]