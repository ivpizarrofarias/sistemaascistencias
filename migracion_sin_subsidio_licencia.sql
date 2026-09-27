-- ========================================================================
-- MIGRACIÓN: EL EMPLEADOR NO PAGA SUBSIDIO DE LICENCIA MÉDICA
-- Base de datos: sistema_asis
-- Ejecutar: mysql -uroot -p'Admin_1983' sistema_asis < migracion_sin_subsidio_licencia.sql
-- ========================================================================
--
-- Regla de negocio
--   El empleador NO paga subsidio por licencia médica: todos los días de
--   licencia quedan a cargo del trabajador. El cálculo de liquidaciones nuevas
--   ya no genera ese haber, por lo que el código (entidad, DTO y mapper) ya no
--   mapea estas columnas.
--
-- POR QUÉ NO SE BORRAN LAS COLUMNAS
--   `liquidaciones.subsidio_licencia_empleador` guarda montos reales de
--   liquidaciones YA emitidas. Borrarla destruiría ese registro histórico, así
--   que se conserva intacta y solo se le da un valor por defecto para que las
--   liquidaciones nuevas (que ya no escriben la columna) se puedan insertar.
--   Lo mismo con `licencias_medicas.employer_pays`: el dato histórico se
--   respeta, pero deja de tener efecto porque el cálculo ya no lo consulta.
--
-- IMPORTANTE
--   * Haz un respaldo antes: mysqldump -uroot -p sistema_asis > respaldo.sql
--   * Es idempotente: se puede ejecutar más de una vez sin efectos duplicados.
--   * No modifica ningún valor existente, solo el DEFAULT de las columnas.
-- ========================================================================

-- 1) Las liquidaciones nuevas no escriben la columna, así que el motor debe
--    tener un valor por defecto (antes fallaba con "no tiene default").
ALTER TABLE `liquidaciones`
    MODIFY COLUMN `subsidio_licencia_empleador` DECIMAL(15, 2) NOT NULL DEFAULT 0.00;

-- 2) Las licencias nuevas tampoco escriben el indicador "el empleador paga".
ALTER TABLE `licencias_medicas`
    MODIFY COLUMN `employer_pays` BIT(1) NOT NULL DEFAULT b'0';

-- 3) Verificación: los datos históricos deben seguir intactos.
--    SELECT id, employee_id, total_haberes, subsidio_licencia_empleador
--      FROM liquidaciones ORDER BY id;
--    SELECT id, start_date, end_date, employer_pays FROM licencias_medicas ORDER BY id;
