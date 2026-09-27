-- ======================================================================
-- MIGRACIÓN: CÓDIGO DE MARCAJE DE 4 DÍGITOS
-- Base de datos: sistema_asis
-- Ejecutar: mysql -uroot -p sistema_asis < migracion_codigo_4_digitos.sql
-- ======================================================================
--
-- El código de marcaje pasa de "4 dígitos del RUT + 4 letras del nombre"
-- (1687JAVI) a un número aleatorio de 4 dígitos (4832).
--
-- Por qué cambia:
--   - el código ya no se deduce del RUT ni del nombre, así que un trabajador
--     no puede adivinar el de un compañero mirando el suyo;
--   - si se corrige el RUT o su dígito verificador, el código NO cambia: es la
--     credencial con la que el trabajador marca asistencia.
--
-- El código es INMUTABLE: se asigna al crear al empleado y nunca se edita.
--
-- ATENCIÓN: esta migración cambia la credencial de marcaje de los empleados que
-- ya tienen el formato anterior. Hay que comunicar el nuevo código a cada
-- trabajador antes de que marque asistencia.
-- ======================================================================

-- ----------------------------------------------------------------------
-- 1. Mostrar los códigos actuales antes de tocarlos
-- ----------------------------------------------------------------------
SELECT id, rut, first_name, last_name, code AS codigo_actual
FROM empleados
ORDER BY id;

-- ----------------------------------------------------------------------
-- 2. Asignar un código de 4 dígitos a los que aún no lo tienen
--    (empleados dados de alta antes de que existiera el código).
--    Los que ya tienen un número válido de 4 dígitos NO se tocan.
-- ----------------------------------------------------------------------
UPDATE empleados
SET code = LPAD(FLOOR(RAND() * 9000) + 1000, 4, '0')
WHERE code IS NULL
   OR code = ''
   OR code NOT REGEXP '^[0-9]{4}$';

-- ----------------------------------------------------------------------
-- 3. Verificación: todos deben tener 4 dígitos y no puede haber repetidos
-- ----------------------------------------------------------------------
SELECT id, rut, first_name, last_name, code
FROM empleados
ORDER BY id;

SELECT code, COUNT(*) AS veces
FROM empleados
GROUP BY code
HAVING COUNT(*) > 1;
-- Si la consulta anterior no devuelve filas, no hay códigos repetidos.
