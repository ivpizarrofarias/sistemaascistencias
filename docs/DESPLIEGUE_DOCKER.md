# Despliegue con Docker (servidor propio)

Este despliegue levanta **solo** el frontend y el backend en contenedores. La
base de datos MySQL `sistema_asis` es externa, en `100.73.41.33:3306`.

## Arquitectura

```
Internet → :8081 → nginx (frontend Angular)
                    └─ /api/* → backend:8080 (Spring Boot)
                                    └─ MySQL externo 100.73.41.33:3306/sistema_asis
```

El frontend usa rutas relativas `/api`, por lo que **no depende de CORS** en
producción: nginx reenvía esas peticiones al backend dentro de la red de Docker.
El backend no publica su puerto 8080 directamente; solo se expone el puerto HTTP
configurado en `HTTP_PORT` (80 por defecto).

## Requisitos en el servidor

- Docker Engine con el plugin `docker compose` (o Docker Desktop).
- Acceso de red desde el servidor a `100.73.41.33:3306`.
- Git (o copiar el proyecto por otro medio).
- Puerto `8081` libre en el servidor (el puerto 80 ya lo usa otra aplicación).
  Si necesitas otro puerto, cámbialo en `HTTP_PORT`.
- Si UFW está activo, permitir el puerto web y el acceso de los contenedores a
  MySQL local (en este servidor ya quedó aplicado):
  ```bash
  sudo ufw allow 8081/tcp
  sudo ufw allow from 172.16.0.0/12 to any port 3306 proto tcp
  ```

## 1. Obtener el código

```bash
git clone https://github.com/ivpizarrofarias/sistemaascistencias.git
cd sistemaascistencias
```

Si el servidor ya tiene el repo, actualízalo antes de desplegar:

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
DB_URL="jdbc:mysql://100.73.41.33:3306/sistema_asis?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Santiago"
DB_USERNAME=dev
DB_PASSWORD=PASSWORD_REAL_DE_MYSQL
JWT_SECRET=secreto_aleatorio_de_al_menos_48_caracteres
ADMIN_USERNAME=admin
ADMIN_PASSWORD=PASSWORD_REAL_DEL_ADMIN
```

Para generar un `JWT_SECRET` aleatorio:

```bash
openssl rand -hex 48
```

> La contraseña de MySQL **no** está en `application.properties`: ese archivo
> solo contiene `${DB_PASSWORD:}`. El valor real se inyecta desde `.env` al
> contenedor del backend.
>
> Si MySQL está en el **mismo servidor** donde corre Docker (como en este
> despliegue), cambia el host en `DB_URL` a `host.docker.internal`:
> `jdbc:mysql://host.docker.internal:3306/sistema_asis?...`. El archivo
> `docker-compose.yml` ya incluye `host-gateway` para que eso funcione. Desde
> otro servidor se usa directamente la IP `100.73.41.33`.

`CORS_ALLOWED_ORIGINS` debe incluir **la URL pública de la app**, aunque el
frontend se sirva desde el mismo nginx (Chrome envía la cabecera `Origin` en las
peticiones POST y Spring la valida; si no está permitida, el login responde 403
`Invalid CORS request` y la pantalla muestra un error `OK`). Separar varios
orígenes con comas:

```dotenv
CORS_ALLOWED_ORIGINS=http://100.73.41.33:8081,http://localhost:*,http://127.0.0.1:*
```

Después de cambiar `.env`, hay que **recrear** los contenedores; un simple
`docker compose restart` no vuelve a leer el archivo:

```bash
docker compose up -d --force-recreate backend
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
docker compose logs -f backend
docker compose logs -f frontend
```

El backend espera a que MySQL acepte conexiones antes de marcarse sano; el
frontend arranca recién después. El healthcheck del backend usa un puerto TCP
interno 8080, que no queda expuesto a Internet.

Acceso web:

```
http://100.73.41.33:8081
```

Si se cambió `HTTP_PORT`, usar `http://IP_DEL_SERVIDOR:HTTP_PORT`.
El usuario inicial por defecto es `admin` / `admin123`, pero debe cambiarse en
`.env` antes de exponer el sistema.

## 4. App Android en producción

La app llama al mismo puerto `8081`: las peticiones `api/*` pasan por el proxy
de nginx hacia el backend. La URL base ya quedó configurada en
`android/app/src/main/java/cl/ipfsoft/backend/app/data/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://100.73.41.33:8081/"
```

No agregar `/api/` a `BASE_URL`, porque los endpoints de `ApiService` ya
incluyen ese prefijo (por ejemplo `api/auth/login`).

La app guarda el JWT en DataStore y un interceptor de OkHttp lo envía como
`Authorization: Bearer <token>` en todas las peticiones. Sin esa cabecera, los
endpoints de consulta (`/api/employees`, `/api/attendance/employee/*`,
`/api/permisos/employee/*`, `/api/payroll/liquidaciones/employee/*`) responden
**401**, aunque el login funcione.

Los usuarios `admin`, `Iván Pizarro` y `Danitza` no tienen `employee_id`
asociado, así que el panel muestra un selector con los empleados del servidor
para elegir de quién se consultan los datos.

Luego recompilar el APK. En producción se recomienda HTTPS; si se usa HTTP,
el dispositivo debe permitir tráfico cleartext (la app ya lo tiene habilitado
para desarrollo).

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

El backup se hace en el MySQL externo, no en los contenedores:

```bash
mysqldump -h 100.73.41.33 -P 3306 -u dev -p sistema_asis > backup_sistema_asis.sql
```

Restaurar:

```bash
mysql -h 100.73.41.33 -P 3306 -u dev -p sistema_asis < backup_sistema_asis.sql
```

Guarda el dump en un lugar seguro y no lo subas al repositorio.

## 7. Recomendaciones de seguridad

- Usar HTTPS delante del puerto 8081 (reverse proxy con certificado
  Let's Encrypt o el balanceador del proveedor).
- Cambiar `ADMIN_PASSWORD` y `JWT_SECRET` antes del primer acceso público.
- Restringir el acceso a MySQL solo desde la IP del servidor de la app.
- No publicar logs con secretos ni commitear `.env`.
- Si usaste un token de GitHub para subir el código, revócalo después del push.

## Solución de problemas

| Síntoma | Revisión |
|---------|----------|
| el backend no arranca y dice `Access denied` | `DB_USERNAME` / `DB_PASSWORD` en `.env`; permisos de `dev` desde la IP del servidor |
| `Communications link failure` | conectividad al puerto 3306, firewall/security group |
| la web carga pero los datos no | `docker compose logs frontend`, `docker compose logs backend`, verificar que `/api` llegue a `backend:8080` |
| 404 al recargar una ruta Angular | confirmar que nginx usa `try_files $uri $uri/ /index.html;` |
| el login muestra un error `OK` y no entra | el backend responde 403 `Invalid CORS request`; agregar la URL pública a `CORS_ALLOWED_ORIGINS` y recrear el contenedor (`up -d --force-recreate`) |
| `Bad Gateway` justo tras recrear | el backend aún está arrancando; esperar a `healthy` en `docker compose ps` |
| el frontend no compila | revisar `frontend/package-lock.json` y logs de `docker compose build frontend` |
