-- ========================================================================
-- MIGRACIÓN: RENOMBRE DE TABLAS DE INGLÉS A ESPAÑOL
-- Base de datos: sistema_asis
-- Ejecutar: mysql -uroot -p'Admin_1983' sistema_asis < migracion_nombres_espanol.sql
-- ========================================================================
--
-- IMPORTANTE
--   * Detén la aplicación antes de ejecutar (para que Hibernate no reescriba
--     el esquema mientras se renombran las tablas).
--   * Las columnas y los datos NO se modifican: solo cambia el nombre de la
--     tabla. Los índices, claves foráneas y auto_increment se conservan.
--   * RENAME TABLE no funciona dentro de una transacción en MySQL: si algo
--     falla a mitad de camino, corrige los RENAME restantes y vuelve a
--     ejecutar; los ya aplicados no se repetirán porque el nombre viejo
--     ya no existe.
--   * Ejecuta este script UNA sola vez. Si ya lo aplicaste, no hay nada
--     que hacer.
-- ========================================================================

-- 1) Empleados
RENAME TABLE `employees` TO `empleados`;

-- 2) Usuarios de la aplicación
RENAME TABLE `app_users` TO `usuarios`;

-- 3) Asistencias
RENAME TABLE `attendances` TO `asistencias`;

-- 4) Horarios semanales
RENAME TABLE `weekly_schedules` TO `horarios_semanales`;

-- 5) Días de cada horario semanal
RENAME TABLE `weekly_schedule_days` TO `dias_horario_semanal`;

-- 6) Anticipos
RENAME TABLE `advances` TO `anticipos`;

-- 7) Parámetros del sistema
RENAME TABLE `system_params` TO `parametros_sistema`;

-- ========================================================================
-- Tablas que YA estaban en español (sin cambios):
--   permisos_laborales, liquidaciones, liquidacion_detalle,
--   licencias_medicas, horas_extra
-- ========================================================================

-- Verificación: deben aparecer las 12 tablas en español.
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY table_name;
