#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if ! command -v docker >/dev/null 2>&1 || ! docker compose version >/dev/null 2>&1; then
    echo "Docker con el plugin 'compose' no está disponible. Instálalo y vuelve a intentar." >&2
    exit 1
fi

if [[ ! -f .env ]]; then
    cp .env.example .env
    echo "Se creó .env desde .env.example."
    echo "Edita DB_PASSWORD, JWT_SECRET y ADMIN_PASSWORD y vuelve a ejecutar este script." >&2
    exit 1
fi

if grep -Eq 'cambiar_password_root_mysql|cambiar_password_mysql|cambiar_este_valor|cambiar_password_admin' .env; then
    echo "Hay valores pendientes de reemplazar en .env (MySQL, JWT_SECRET o ADMIN_PASSWORD)." >&2
    exit 1
fi

echo "Levantando backend y frontend..."
docker compose up -d --build

echo
echo "Estado de los contenedores:"
docker compose ps

echo
echo "Aplicación disponible en http://localhost:${HTTP_PORT:-8081}"
