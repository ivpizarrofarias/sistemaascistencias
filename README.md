# Sistema de Asistencias y Liquidaciones (Chile)

Sistema completo de gestión de asistencia y liquidaciones de sueldos, con lógica
de legislación laboral chilena (jornadas con colación, permisos por hora/día,
compensación, asignación familiar, AFP/SIS/salud/cesantía, impuesto único).

El proyecto tiene tres partes:

| Carpeta    | Descripción                                             |
|------------|---------------------------------------------------------|
| `backend/` | API REST (Spring Boot 4.0.8, Java 17, MySQL por defecto) |
| `frontend/`| Aplicación web (React + Vite + TypeScript)              |
| `android/` | App Android de marcaje (Kotlin + Jetpack Compose)       |

---

## Requisitos

- **Backend**: Java 17, Maven (`./mvnw`), MySQL 8.
- **Frontend**: Node 18+ y npm.
- **Android**: Android Studio (LadyBug o superior), JDK 17.

---

## 1. Backend (API REST)

1. Crear la base de datos en MySQL (usuario/contraseña según
   `backend/src/main/resources/application.properties`):
   ```sql
   CREATE DATABASE sistema_asis;
   ```
2. Ajustar `spring.datasource.password` en
   `backend/src/main/resources/application.properties`.
3. Ejecutar (desde `backend/`):
   ```bash
   ./mvnw spring-boot:run
   ```
   El servidor queda en `http://localhost:8080`.

   Usuario admin por defecto: `admin` / `admin123`.

4. Ejecutar los tests (52 tests de unidad e integración, usan H2):
   ```bash
   ./mvnw test
   ```

---

## 2. Frontend (web)

Desde `frontend/`:

```bash
npm install
npm run dev        # http://localhost:5173
```

El Vite está configurado con proxy `/api` → `http://localhost:8080`, así que
debe estar corriendo el backend. Para producción:

```bash
npm run build
```

Incluye un **kiosco público** (`/mark`) para que los trabajadores marquen su
asistencia por RUT, paneles de administración (empleados, asistencia, permisos,
liquidaciones) y portal del empleado (mis permisos, mi asistencia, mis
liquidaciones).

---

## 3. App Android (marcaje)

El proyecto Android está en `android/`. No se puede compilar en este entorno
(no hay Android SDK instalado), por lo que se abre y compila desde
**Android Studio**:

1. Abrir la carpeta `android/` con Android Studio (importar como proyecto Gradle).
2. Android Studio descargará Gradle 8.9 y las dependencias automáticamente.
3. Ejecutar sobre un emulador o dispositivo físico.

La base URL de la API está en
`android/app/src/main/java/cl/ipfsoft/backend/app/data/RetrofitClient.kt`:

- `http://10.0.2.2:8080/` → `localhost` de la máquina vista desde el **emulador**.
- Para **dispositivo físico**, cambiar por la IP local de la máquina
  (p. ej. `http://192.168.1.10:8080/`).

### Funcionalidades de la app

- **Kiosco**: marcaje público por RUT (entrada, colaciones y salida), con
  indicación del siguiente paso del día.
- **Portal del empleado**: login con usuario/contraseña, consulta de su propia
  asistencia, permisos y liquidaciones (token JWT guardado con DataStore).

### Notas técnicas Android

- Kotlin 2.0.20, AGP 8.5.2, Compose BOM 2024.09.03, minSdk 24 / targetSdk 35.
- `usesCleartextTraffic=true` para permitir HTTP en desarrollo; en producción
  debe usarse HTTPS.
- No se incluye el wrapper `gradlew` (Android Studio lo genera al abrir el
  proyecto); se aporta el `gradle/wrapper/gradle-wrapper.properties`.

---

## Contratos de la API consumidos por el frontend/Android

- `POST /api/auth/login` — iniciar sesión.
- `POST /api/attendance/mark` (público) — marcar el siguiente paso por RUT.
- `POST /api/attendance/status` (público) — consultar estado del día.
- `GET /api/attendance/employee/{id}` — asistencias de un empleado.
- `GET /api/permisos/employee/{id}` — permisos de un empleado.
- `GET /api/payroll/liquidaciones/employee/{employeeId}` — liquidaciones de un empleado.
- CRUD de empleados, asistencia (admin), permisos (admin/empleado) y
  liquidaciones (admin) según las reglas de negocio del sistema.
# sistemaascistencias
