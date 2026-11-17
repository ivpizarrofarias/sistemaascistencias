# ---- Build: compilar Angular ----
FROM node:20-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Runtime: nginx sirve el frontend y hace proxy de /api ----
FROM nginx:1.27-alpine
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/sistema-asistencias/browser /usr/share/nginx/html

EXPOSE 80
