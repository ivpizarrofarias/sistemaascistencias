-- ============================================================================
-- Migración: monto en horas_extra
-- ============================================================================
-- El monto de las horas extra ahora lo ingresa el administrador y se suma
-- tal cual a la liquidación, en vez de calcularse con
-- valor_hora x horas x multiplicador.
--
-- La columna `horas` se conserva como dato informativo (cuántas horas se
-- trabajaron de más); el dinero a pagar es `monto`.
-- ============================================================================

-- 1. Agregar la columna `monto` si no existe.
--    DEFAULT 0 para que los registros previos (sin monto) no bloqueen la carga.
SET @existe_monto := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'horas_extra'
      AND COLUMN_NAME = 'monto'
);

SET @sql := IF(
    @existe_monto > 0,
    'SELECT ''La columna horas_extra.monto ya existe'' AS info',
    'ALTER TABLE horas_extra ADD COLUMN monto DECIMAL(15,2) NOT NULL DEFAULT 0.00 AFTER horas'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Los registros que quedaron con monto 0.00 tienen horas registradas pero
--    ningún monto. Revísalos y corrígelos desde la aplicación (botón
--    "Horas extra") o con un UPDATE explícito, por ejemplo:
--
--    UPDATE horas_extra SET monto = 20000 WHERE id = 1;
--
--    ADVERTENCIA: no hay forma de recuperar el monto original, porque antes
--    no se guardaba. Cualquier valor debe ingresarlo knowingly el usuario.
