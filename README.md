# Sistema de Asistencias y Liquidaciones (Chile)

Sistema completo de gestión de asistencia y liquidaciones de sueldos, con lógica
de legislación laboral chilena (jornadas con colación, permisos por hora/día,
compensación, asignación familiar, AFP/SIS/salud/cesantía, impuesto único).

El proyecto tiene tres partes:

| Carpeta    | Descripción                                             |
|------------|---------------------------------------------------------|
| `backend/` | API REST (Spring Boot 4.0.8, Java 17, MySQL por defecto) |
| `frontend/`| Aplicación web (Angular 21 + Angular Material)        |
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
3. Si ya tenías la base creada con los nombres de tabla en inglés, ejecuta
   **una sola vez** el script de migración (con la aplicación detenida):
   ```bash
   mysql -uroot -p sistema_asis < migracion_nombres_espanol.sql
   ```
4. Ejecutar (desde `backend/`):
   ```bash
   ./mvnw spring-boot:run
   ```
   El servidor queda en `http://localhost:8080`.

   Usuario admin por defecto: `admin` / `admin123`.

5. Ejecutar los tests (75 tests de unidad e integración, usan H2):
   ```bash
   ./mvnw test
   ```

### Modelo de datos

Todas las entidades JPA y sus tablas están en español:

| Entidad           | Tabla                     |
|-------------------|---------------------------|
| `Empleado`        | `empleados`               |
| `Usuario`         | `usuarios`                |
| `Asistencia`      | `asistencias`             |
| `HorarioSemanal`  | `horarios_semanales`      |
| `DiaHorario`      | `dias_horario_semanal`    |
| `PermisoLaboral`  | `permisos_laborales`      |
| `Liquidacion`     | `liquidaciones`           |
| `LiquidacionDetalle` | `liquidacion_detalle`   |
| `Anticipo`        | `anticipos`               |
| `LicenciaMedica`  | `licencias_medicas`       |
| `HoraExtra`       | `horas_extra`             |
| `ParametroSistema`| `parametros_sistema`      |

Los DTOs y los contratos JSON de la API conservan sus nombres en inglés
(`EmployeeResponse`, `AttendanceResponse`, …) para no romper los clientes
(frontend y app Android).

### Reglas de negocio

- **Licencias médicas**: el empleador no paga la licencia. Todos los días
  solapados de una licencia se descuentan del sueldo del trabajador, por lo que
  no existe un haber de subsidio patronal. Las columnas
  `licencias_medicas.employer_pays` y
  `liquidaciones.subsidio_licencia_empleador` se conservan en la base de datos
  solo por compatibilidad (`migracion_sin_subsidio_licencia.sql` las deja con
  default `0`), pero el código y la API ya no las utilizan.
- **Control de asistencia**: la vista es maestro/detalle. Se listan todos los
  empleados y, al seleccionar uno, se muestra su asistencia del período. No
  existe un endpoint masivo: cada consulta va a
  `GET /api/attendance/employee/{id}`.
- **Liquidación de sueldo**: el anticipo del período se descuenta junto con el
  resto de los descuentos y aparece como una línea normal
  `Anticipo de sueldo — DESCUENTO`, sin una sección aparte.

---

## 2. Frontend (web)

Desde `frontend/`:

```bash
npm install
npm start           # http://localhost:4200
```

Angular CLI está configurado con proxy `/api` → `http://localhost:8080`
(`proxy.conf.json`), así que debe estar corriendo el backend. Para producción:

```bash
npm run build       # salida en dist/sistema-asistencias
```

Incluye un **dashboard** con KPIs y gráficos de asistencia del personal, y
paneles de administración: empleados, horarios semanales (con descarga de PDF),
control de asistencia, permisos laborales y remuneraciones (liquidaciones,
anticipos, licencias médicas y horas extra). El marcaje por RUT se hace desde la
app Android.

### Stack

- Angular 21 (standalone components, signals, zoneless).
- Angular Material 21 con tema M3 y `ng2-charts` + Chart.js para el dashboard.
- Carga perezosa por ruta (`loadComponent`) y servicios con `HttpClient`.

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
- `GET /api/payroll/liquidaciones/{id}/pdf` — PDF de la liquidación, en una sola
  hoja, con el bloque de firma del trabajador y nombre de archivo
  `Liquidación_<Empleado>_<Mes>_<Año>.pdf` (UTF-8 vía `filename*`, con fallback
  ASCII para clientes antiguos).
- CRUD de empleados, asistencia (admin), permisos (admin/empleado) y
  liquidaciones (admin) según las reglas de negocio del sistema.
