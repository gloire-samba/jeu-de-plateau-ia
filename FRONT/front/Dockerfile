# ==========================================
# ÉTAPE 1 : BUILD
# ==========================================
FROM node:24-alpine AS builder
WORKDIR /app
COPY package*.json ./
# On force l'installation pour contourner le bug Node 24
RUN npm install --legacy-peer-deps
RUN npm install zone.js @types/node --legacy-peer-deps
COPY . .
RUN npm run build -- --configuration production

# ==========================================
# ÉTAPE 2 : NGINX (Production)
# ==========================================
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist/front/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]