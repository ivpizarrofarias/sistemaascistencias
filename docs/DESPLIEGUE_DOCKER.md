# Despliegue con Docker Compose

Este despliegue levanta **solo** el frontend y el backend en contenedores. La
base de datos MySQL `sistema_asis` se ejecuta fuera de los contenedores (en la
máquina host o en otro equipo de la red local).

## Arquitectura

```
Navegador → http://localhost:8081 → nginx (frontend Angular)
                                     └─ /api/* → backend:8080 (Spring Boot)
                                                   └─ MySQL en el host:3306/sistema_asis
```

El frontend usa rutas relativas `/api`, por lo que nginx reenvía esas peticiones
al backend dentro de la red de Docker. El backend no publica su puerto 8080
directamente: solo se expone el puerto HTTP configurado en `HTTP_PORT` (8081 por
defecto en el ejemplo).

## Requisitos

- Docker Engine con el plugin `docker compose` (o Docker Desktop).
- MySQL 8 en ejecución con la base de datos `sistema_asis` creada.
- Git (o copiar el proyecto por otro medio).
- El puerto definido en `HTTP_PORT` libre en la máquina.

> Si MySQL corre en la misma máquina donde está Docker, `docker-compose.yml`
> ya incluye `extra_hosts: host.docker.internal:host-gateway`, así que el
> contenedor puede conectarse con `host.docker.internal:3306`.

## 1. Obtener el código

```bash
git clone https://github.com/ivpizarrofarias/sistemaascistencias.git
cd sistemaascistencias
```

Si el repo ya existe, actualízalo antes de desplegar:

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
DB_URL="jdbc:mysql://host.docker.internal:3306/sistema_asis?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Santiago"
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

> La contraseña de MySQL **no** está en `application.properties`: ese archivo
> solo contiene `${DB_PASSWORD:}`. El valor real se inyecta desde `.env` al
> contenedor del backend.

`CORS_ALLOWED_ORIGINS` debe incluir la URL desde la que abras la app, aunque el
frontend se sirva desde el mismo nginx (Chrome envía la cabecera `Origin` en las
peticiones POST y Spring la valida; si no está permitida, el login responde 403
`Invalid CORS request` y la pantalla muestra un error `OK`). Separar varios
orígenes con comas:

```dotenv
CORS_ALLOWED_ORIGINS=http://localhost:8081,http://127.0.0.1:8081
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
frontend arranca recién después. El healthcheck del backend usa el puerto TCP
interno 8080, que no queda expuesto.

Acceso web:

```
http://localhost:8081
```

Si se cambió `HTTP_PORT`, usar `http://localhost:HTTP_PORT`. Las credenciales
del administrador se definen en `.env` con `ADMIN_USERNAME` y `ADMIN_PASSWORD`.

## 4. App Android

El repo queda configurado para desarrollo local. La URL base está en
`android/app/src/main/java/cl/ipfsoft/backend/app/data/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

- `10.0.2.2:8080` es el `localhost` de la máquina visto desde el emulador y
  requiere el backend corriendo en `localhost:8080`.
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

El backup se hace en MySQL, no en los contenedores:

```bash
mysqldump -h 127.0.0.1 -P 3306 -u dev -p sistema_asis > backup_sistema_asis.sql
```

Restaurar:

```bash
mysql -h 127.0.0.1 -P 3306 -u dev -p sistema_asis < backup_sistema_asis.sql
```

Guarda el dump en un lugar seguro y no lo subas al repositorio.

## 7. Recomendaciones de seguridad

- No publicar logs con secretos ni commitear `.env`.
- Cambiar `ADMIN_PASSWORD` y `JWT_SECRET` antes del primer acceso.
- Usar HTTPS delante del puerto HTTP si la aplicación se expone fuera de la red local.
- Restringir el acceso a MySQL solo desde la máquina donde corre la aplicación.

## Solución de problemas

| Síntoma | Revisión |
|---------|----------|
| el backend no arranca y dice `Access denied` | `DB_USERNAME` / `DB_PASSWORD` en `.env`; permisos del usuario desde el host |
| `Communications link failure` | MySQL en `localhost:3306`, `host.docker.internal` y firewall |
| la web carga pero los datos no | `docker compose logs frontend`, `docker compose logs backend`, verificar que `/api` llegue a `backend:8080` |
| 404 al recargar una ruta Angular | confirmar que nginx usa `try_files $uri $uri/ /index.html;` |
| el login muestra un error `OK` y no entra | el backend responde 403 `Invalid CORS request`; agregar la URL usada a `CORS_ALLOWED_ORIGINS` y recrear el contenedor (`up -d --force-recreate`) |
| `Bad Gateway` justo tras recrear | el backend aún está arrancando; esperar a `healthy` en `docker compose ps` |
| el frontend no compila | revisar `frontend/package-lock.json` y logs de `docker compose build frontend` |
