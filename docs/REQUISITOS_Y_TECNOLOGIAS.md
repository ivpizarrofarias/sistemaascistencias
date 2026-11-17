# Sistema de Asistencias y Liquidaciones - Requisitos y Tecnologías

## 1. Descripción general
Sistema integral para gestión de asistencia, horarios, permisos laborales y remuneraciones (liquidaciones) orientado a la legislación laboral chilena. Incluye:
- **Backend**: API REST con autenticación JWT.
- **Frontend web**: panel administrativo y de consulta.
- **App Android**: marcaje de asistencia y portal del empleado.

## 2. Tecnologías utilizadas

### Backend
- **Lenguaje:** Java 17.0.15 LTS
- **Framework:** Spring Boot 4.0.8 (Web MVC, Data JPA, Validation, Security)
- **Seguridad:** Spring Security + JWT (`jjwt` 0.12.6), contraseñas con BCrypt, control de acceso por roles (`@PreAuthorize`)
- **Persistencia:** Spring Data JPA / Hibernate, MySQL 8.4 (producción/desarrollo), H2 (tests)
- **Base de datos:** MySQL (`sistema_asis`), pool HikariCP
- **PDF:** OpenPDF 1.3.30 (liquidaciones/horarios)
- **Build:** Maven (`mvnw`), Lombok
- **Otros:** manejo global de errores (`@RestControllerAdvice`), DTOs y mappers, utilidades (RUT, horarios)

### Frontend (Web)
- **Framework:** Angular 21.2 (standalone components, signals, zoneless), Node.js 22.23.4 / npm 10.9.9
- **UI:** Angular Material 21 (tema M3), SCSS
- **Gráficos:** `ng2-charts` + Chart.js (dashboard)
- **Comunicación:** `HttpClient`, interceptor JWT, proxy dev `/api` → backend
- **Ruteo:** carga perezosa por ruta (`loadComponent`), guards por autenticación/rol
- **Notificaciones:** SweetAlert2 (toasts/mensajes)
- **Build:** Angular CLI / npm

### App Android
- **Lenguaje:** Kotlin 2.0.20
- **UI:** Jetpack Compose (Material 3), Compose BOM 2024.09.03
- **Arquitectura/UI state:** ViewModel, StateFlow
- **Red:** Retrofit 2.11 + OkHttp 4.12 + Gson 2.11 (consumo API REST), JWT enviado en `Authorization: Bearer`
- **Almacenamiento local:** DataStore 1.1.1 (sesión/token)
- **Build:** Gradle 8.9, AGP 8.5.2, `minSdk 24`, `targetSdk 35`, `compileSdk 35`

### Docker
- Docker Engine con plugin `docker compose` (o Docker Desktop).
- `docker-compose.yml`: servicios `mysql:8.4`, backend y frontend; puertos
  configurables (`MYSQL_HOST_PORT`, `BACKEND_PORT`, `HTTP_PORT`) y volumen
  persistente `mysql_data`.
- `backend/Dockerfile`: multi-stage `maven:3.9-eclipse-temurin-17` →
  `eclipse-temurin:17-jre-jammy`, JAR en `/app/app.jar`, usuario no root.
- `frontend/Dockerfile`: multi-stage `node:20-alpine` → `nginx:1.27-alpine`,
  sirve `dist/sistema-asistencias/browser`.
- `frontend/nginx.conf`: proxy `/api/` a `backend:8080` y fallback SPA.
- `scripts/deploy.sh`: verifica Docker/`.env` y levanta los servicios.
- Guía completa: `docs/DESPLIEGUE_DOCKER.md`.

### Infraestructura/entorno
- **Docker Engine + Docker Compose** para levantar MySQL, backend y frontend en contenedores
- API por defecto en `http://localhost:8080`
- Frontend dev en `http://localhost:4200` con proxy a `/api`
- Android emulador usa `http://10.0.2.2:8080/` para acceder al backend local
  (valor por defecto en `RetrofitClient.kt`)
- Con Docker Compose la app queda en `http://localhost:8081` (proxy `/api` al
  backend)

## 3. Requisitos funcionales (qué puede hacer el sistema)

### Autenticación y usuarios
- Inicio de sesión con usuario/contraseña y emisión de token JWT.
- Perfil de usuario: actualización de nombre de usuario, nombre completo, foto y contraseña (persistido en BD).
- Avatar de usuario para pantalla de login (endpoint público de avatar).
- Gestión de usuarios del sistema (según permisos): crear/editar usuarios con roles **ADMIN** y **SUPERVISOR**, habilitar/deshabilitar.

### Roles y permisos
- **ADMIN:** acceso completo de administración (gestión de empleados, horarios, asistencias, permisos, remuneraciones y usuarios del sistema; acciones críticas como habilitar/eliminar según configuración).
- **SUPERVISOR:** acceso operativo/administrativo amplio, sin permisos para habilitar/deshabilitar o borrar donde esté restringido a ADMIN.
- **EMPLOYEE:** acceso a su propia información (asistencia, permisos y liquidaciones) y operaciones asociadas a su cuenta según corresponda.

### Empleados
- CRUD de empleados (datos personales/laborales, RUT, fecha de ingreso, sueldo base, AFP/salud, cargas, etc.).
- Validación de RUT en creación/edición.
- Habilitar/deshabilitar y eliminar empleados (acciones restringidas por rol).

### Horarios
- Gestión de horarios semanales por empleado (días/jornadas, colaciones).
- Consulta/creación/actualización/eliminación de horarios.
- Autoformato de horas al ingresar (ej. `0730` → `07:30`).
- Generación/descarga de PDF de horario semanal.

### Asistencia y marcaje
- Marcaje por código (endpoint público para kiosco/app): registra secuencia del día (entrada, colaciones, salida) y consulta estado del día.
- Consulta de asistencias por empleado y rango de fechas.
- Registro/corrección administrativa de asistencias (completar, ajustar, desbloquear situaciones).
- Marcación de ausencias e indicación de licencias médicas en registros de asistencia.

### Permisos laborales
- Solicitud de permisos por horas o por días.
- Flujo de aprobación/rechazo/validación por administración.
- Compensación/devolución de tiempo o descuento según corresponda.
- Consulta por empleado y listado general (según rol).

### Licencias médicas, horas extra y anticipos
- Registro y gestión de licencias médicas por empleado y período.
- Registro y gestión de horas extra por fecha.
- Registro y gestión de anticipos de sueldo (con reglas de negocio/tope según período).

### Remuneraciones / Liquidaciones
- Generación/recalculo de liquidación mensual por empleado y liquidación masiva del período.
- Cálculo de haberes y descuentos con lógica de remuneraciones chilenas (sueldo base, gratificación, asignación familiar por tramos/cargas, imposiciones como AFP/salud/cesantía, impuesto único, descuentos por inasistencias/licencias/permisos/anticipos, horas extra según registros).
- Consulta de liquidaciones por empleado y detalle de liquidación.
- Marcado de liquidación como pagada.
- Descarga de liquidación en PDF.

### Dashboard y reportes visuales
- Dashboard web con KPIs e indicadores/gráficos de asistencia del período (incluye visualización por empleado).

### App Android
- Pantalla de **marcaje de asistencia** por código (UI dedicada, mantiene pantalla activa durante uso).
- **Portal del empleado**: inicio de sesión y consulta de asistencias, permisos y liquidaciones propias.

## 4. Requisitos no funcionales y seguridad (implementado / considerado)
- Autenticación stateless con JWT y autorización por roles.
- Contraseñas almacenadas con hash BCrypt.
- Validación de datos de entrada en API (`@Valid` / Bean Validation) y reglas de negocio en servicios.
- Acceso a datos mediante JPA/consultas parametrizadas (sin concatenación de SQL dinámico en repositorios).
- CORS configurado para orígenes de desarrollo local (debe ajustarse por entorno en producción).
- Manejo centralizado de errores con respuestas uniformes.

## 5. Lo que el sistema NO hace (fuera de alcance / no implementado)
- **No** reemplaza asesoría legal/tributaria/laboral: los cálculos siguen reglas parametrizadas y lógica implementada, pero requieren validación normativa/contable según la realidad de cada empresa.
- **No** incluye portal de autoservicio web completo para empleados (el autoservicio móvil es de consulta/marcaje según lo implementado).
- **No** gestiona pagos bancarios/transferencias reales ni integración con sistemas externos (previred, bancos, SII, RRHH externos) fuera de lo expuesto por la API.
- **No** incluye firma electrónica avanzada ni workflow documental complejo más allá de PDFs generados.
- **No** garantiza por sí solo cumplimiento productivo sin configuración segura: requiere HTTPS, gestión de secretos, CORS de producción, backups/monitoreo y endurecimiento de infraestructura.
- **No** implementa biometría, geolocalización, reconocimiento facial ni control de dispositivo avanzado para marcaje (el marcaje es por código).
- **No** incluye multiempresa/multitenencia, internacionalización completa ni configuración dinámica de todas las normas laborales sin cambios de código/parámetros.
- **No** conecta el frontend ni la app Android directamente a MySQL: solo el backend accede a la BD.
- **No** envía notificaciones push, correo ni SMS; **no** tiene modo offline ni sincronización diferida.
- **No** exporta a Excel (los reportes son PDF); **no** incluye recuperación de contraseña por correo, rate limiting ni auditoría avanzada sin trabajo adicional.

## 6. Módulos y entidades principales (dominio)
- Empleados, Usuarios/Roles
- Horarios semanales y días de horario
- Asistencias
- Permisos laborales
- Licencias médicas, Horas extra, Anticipos
- Liquidaciones y detalle de liquidación
- Parámetros del sistema

## 7. Diagramas
- Arquitectura general: `docs/imagenes/arquitectura.svg`
- Módulos y funcionalidades: `docs/imagenes/modulos.svg`
- Flujo de marcaje de asistencia: `docs/imagenes/flujo-marcaje.svg`

## 8. Respaldo de base de datos
- Archivo: `docs/db/backup_sistema_asis_sin_usuarios_privados.sql`
- Contiene estructura y datos del esquema `sistema_asis` **excluyendo** los usuarios del sistema **Danitza Flores** e **Iván Pizarro** (tabla `usuarios`).
- No incluye credenciales de conexión de la base de datos.

## 9. Consideraciones para despliegue/producción
- Externalizar secretos y credenciales (BD, JWT) por variables de entorno/secret manager.
- Usar HTTPS extremo a extremo y deshabilitar tráfico claro en Android para producción.
- Configurar CORS con orígenes explícitos del frontend productivo.
- Usar migraciones controladas de base de datos (en lugar de `ddl-auto=update`) y políticas de respaldo/monitoreo.
- Revisar y aplicar hardening adicional (rate limiting en endpoints públicos, auditoría, retención de logs segura).
