-- Agrega columnas de perfil al usuario (nombre completo y foto en base64).
-- Ejecutar en la base de datos del sistema.

ALTER TABLE usuarios
  ADD COLUMN IF NOT EXISTS full_name VARCHAR(120) NULL,
  ADD COLUMN IF NOT EXISTS foto_base64 LONGTEXT NULL;
