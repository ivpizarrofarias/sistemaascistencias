# Despliegue con Docker Compose

Este despliegue levanta **los tres servicios** en contenedores: MySQL, backend y
frontend. No depende de un MySQL instalado en la máquina.

## Arquitectura

```
Navegador → http://localhost:8081 → nginx (frontend Angular)
                                     └─ /api/* → backend:8080 (Spring Boot)
                                                   └─ mysql:3306 (MySQL 8.4)
```

- MySQL: base `sistema_asis`, usuario `dev`, volumen persistente `mysql_data`.
- Backend: disponible en `localhost:8080` para el emulador Android y el proxy de
  Angular en desarrollo; además, dentro de la red de Compose como
  `backend:8080`.
- Frontend: nginx en `localhost:8081`, con proxy `/api` al backend.

## Requisitos

- Docker Engine con plugin `docker compose` (o Docker Desktop).
- Puertos `3307`, `8080` y `8081` libres en la máquina (configurables en `.env`).
- Git (o copiar el proyecto por otro medio).

> El MySQL del sistema en `localhost:3306` no interfiere: el contenedor publica
> el puerto host `3307`.

## 1. Obtener el código

```bash
git clone https://github.com/ivpizarrofarias/sistemaascistencias.git
cd sistemaascistencias
```

Si el repo ya existe:

```bash
git pull
```

## 2. Configurar variables de entorno

La plantilla está en `.env.example`. **No subir `.env` a GitHub**: ya está
ignorado por `.gitignore`.

```bash
cp .env.example .env
nano .env
```

Valores mínimos que revisar:

```dotenv
HTTP_PORT=8081
BACKEND_PORT=8080
MYSQL_HOST_PORT=3307

MYSQL_ROOT_PASSWORD=PASSWORD_REAL_DE_ROOT
MYSQL_DATABASE=sistema_asis
MYSQL_USER=dev
MYSQL_PASSWORD=PASSWORD_REAL_DE_MYSQL

DB_URL="jdbc:mysql://mysql:3306/sistema_asis?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Santiago"
DB_USERNAME=dev
DB_PASSWORD=PASSWORD_REAL_DE_MYSQL
JWT_SECRET=secreto_aleatorio_de_al_menos_48_caracteres
ADMIN_USERNAME=admin
ADMIN_PASSWORD=PASSWORD_REAL_DEL_ADMIN
CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*
```

Para generar un `JWT_SECRET` aleatorio:

```bash
openssl rand -hex 48
```

> `DB_PASSWORD` debe coincidir con `MYSQL_PASSWORD`. El backend espera a que
> MySQL esté sano antes de arrancar; el frontend espera al backend.

`CORS_ALLOWED_ORIGINS` debe incluir la URL desde la que abras la app, aunque el
frontend se sirva desde el mismo nginx (Chrome envía la cabecera `Origin` en las
peticiones POST y Spring la valida; si no está permitida, el login responde 403
`Invalid CORS request` y la pantalla muestra un error `OK`). Separar varios
orígenes con comas.

Después de cambiar `.env`, hay que **recrear** los contenedores; un simple
`docker compose restart` no vuelve a leer el archivo:

```bash
docker compose up -d --force-recreate
```

## 3. Levantar los servicios

```bash
docker compose up -d --build
```

O, de forma resumida, usar el script incluido (verifica Docker y que `.env` no
tenga valores pendientes):

```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

Verificar estado y logs:

```bash
docker compose ps
docker compose logs -f mysql
docker compose logs -f backend
docker compose logs -f frontend
```

Accesos:

| Servicio | URL |
|----------|-----|
| Web (nginx) | `http://localhost:8081` |
| API backend | `http://localhost:8080/api/...` |
| MySQL (host) | `localhost:3307` (usuario `dev`) |

El esquema se crea automáticamente con `JPA_DDL_AUTO=update` y el usuario
administrador se inicializa con `ADMIN_USERNAME` / `ADMIN_PASSWORD`.

## 4. App Android

El repo queda configurado para desarrollo local. La URL base está en
`android/app/src/main/java/cl/ipfsoft/backend/app/data/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

- `10.0.2.2:8080` es el `localhost` de la máquina visto desde el emulador y
  coincide con `BACKEND_PORT` del Compose.
- En un dispositivo físico, cambiar por la IP de la red local
  (p. ej. `http://192.168.1.10:8080/`).
- No agregar `/api/` a `BASE_URL`, porque los endpoints de `ApiService` ya
  incluyen ese prefijo (por ejemplo `api/auth/login`).

La app guarda el JWT en DataStore y un interceptor de OkHttp lo envía como
`Authorization: Bearer <token>` en todas las peticiones. Sin esa cabecera, los
endpoints de consulta (`/api/employees`, `/api/attendance/employee/*`,
`/api/permisos/employee/*`, `/api/payroll/liquidaciones/employee/*`) responden
**401**, aunque el login funcione.

Los usuarios `admin`, `Iván Pizarro` y `Danitza` no tienen `employee_id`
asociado, así que el panel muestra un selector con los empleados para elegir de
quién se consultan los datos.

Si se usa HTTP, el dispositivo debe permitir tráfico cleartext (la app ya lo
tiene habilitado para desarrollo). En un entorno expuesto se recomienda HTTPS.

## 5. Actualizaciones

```bash
cd sistemaascistencias
git pull
docker compose up -d --build
```

Si cambió solo `.env`, basta con recrear los contenedores:

```bash
docker compose up -d --force-recreate
```

## 6. Backup de la base de datos

El volumen `mysql_data` conserva los datos. Para exportar:

```bash
docker compose exec -T mysql mysqldump -u dev -p sistema_asis > backup_sistema_asis.sql
```

Restaurar:

```bash
docker compose exec -T mysql mysql -u dev -p sistema_asis < backup_sistema_asis.sql
```

Guarda el dump en un lugar seguro y no lo subas al repositorio.

## 7. Recomendaciones de seguridad

- No publicar logs con secretos ni commitear `.env`.
- Cambiar `ADMIN_PASSWORD`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` y
  `JWT_SECRET` antes del primer acceso.
- Usar HTTPS delante del puerto HTTP si la aplicación se expone fuera de la red local.
- No exponer el puerto `3307` fuera de la máquina de desarrollo.

## Solución de problemas

| Síntoma | Revisión |
|---------|----------|
| el backend no arranca y dice `Access denied` | `DB_USERNAME` / `DB_PASSWORD` en `.env`; deben coincidir con `MYSQL_USER` / `MYSQL_PASSWORD` |
| `Communications link failure` | revisar que el servicio `mysql` esté `healthy` en `docker compose ps` |
| el puerto 3307 está ocupado | cambiar `MYSQL_HOST_PORT` en `.env` y recrear |
| la web carga pero los datos no | `docker compose logs frontend`, `docker compose logs backend`, verificar que `/api` llegue a `backend:8080` |
| 404 al recargar una ruta Angular | confirmar que nginx usa `try_files $uri $uri/ /index.html;` |
| el login muestra un error `OK` y no entra | el backend responde 403 `Invalid CORS request`; agregar la URL usada a `CORS_ALLOWED_ORIGINS` y recrear el contenedor (`up -d --force-recreate`) |
| `Bad Gateway` justo tras recrear | el backend aún está arrancando; esperar a `healthy` en `docker compose ps` |
| el frontend no compila | revisar `frontend/package-lock.json` y logs de `docker compose build frontend` |
