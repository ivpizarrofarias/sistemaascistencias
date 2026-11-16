-- Permite almacenar el rol SUPERVISOR (y evita truncado si la columna era ENUM o VARCHAR corto).
-- Ejecutar en la base de datos del sistema (MySQL/MariaDB).

ALTER TABLE usuarios
  MODIFY COLUMN role VARCHAR(20) NOT NULL;

-- Verificación opcional:
-- SHOW COLUMNS FROM usuarios LIKE 'role';
