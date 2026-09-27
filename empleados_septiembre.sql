-- ======================================================================
-- 2 EMPLEADOS CON MARCACIÓN DEL 1 AL 22 DE SEPTIEMBRE DE 2026
-- Base de datos: sistema_asis
-- Ejecutar: mysql -uroot -p sistema_asis < empleados_septiembre.sql
-- ======================================================================
--
-- El código de marcaje son 4 dígitos, único e inmutable: lo genera el sistema
-- al dar de alta al empleado y no se modifica después.
--
-- El 1 de septiembre de 2026 es martes, por lo que el rango abarca 4 semanas:
--   semana del lunes 31/08/2026  (31/08 - 05/09)
--   semana del lunes 07/09/2026  (07/09 - 12/09)
--   semana del lunes 14/09/2026  (14/09 - 19/09)
--   semana del lunes 21/09/2026  (21/09 - 26/09)
--
-- Días con marcación: 16 días hábiles por empleado (L-V).
-- Sábado y domingo no se marcan (no forman parte del horario).
--
-- Cada empleado tiene: 1 día de ausencia injustificada, 1 licencia médica
-- y 1 día incompleto (marcó entrada y colación, le faltó la salida).
-- El día incompleto es el último del rango: dejarlo antes bloquearía el
-- marcaje de los días siguientes por la máquina de estados del kiosco.
-- ======================================================================

-- ======================================================================
-- Javiera Muñoz — RUT 16876543-6 — código de marcaje 4832
-- ======================================================================

INSERT INTO empleados
  (afp_fund, base_salary, dependents, email, enabled, first_name,
   health_system, hire_date, last_name, pension_system, phone,
   rol_economico, rut, code)
VALUES
  ('HABITAT', 980000.00, 1, 'javiera.munoz@empresa.cl', 1, 'Javiera',
   'FONASA', '2023-05-02', 'Muñoz', 'AFP', '961234567', '9', '16876543-6', '4832');

SELECT id INTO @emp_jav FROM empleados WHERE rut = '16876543-6';

-- Horario semana 1: L-V 09:00:00-18:00:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', @emp_jav);
SET @hs_1 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '18:00:00', '09:00:00', @hs_1),
  (2, 1, '18:00:00', '09:00:00', @hs_1),
  (3, 1, '18:00:00', '09:00:00', @hs_1),
  (4, 1, '18:00:00', '09:00:00', @hs_1),
  (5, 1, '18:00:00', '09:00:00', @hs_1),
  (6, 0, NULL, NULL, @hs_1);

-- Horario semana 2: L-V 09:00:00-18:00:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana B', '2026-09-07', @emp_jav);
SET @hs_2 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '18:00:00', '09:00:00', @hs_2),
  (2, 1, '18:00:00', '09:00:00', @hs_2),
  (3, 1, '18:00:00', '09:00:00', @hs_2),
  (4, 1, '18:00:00', '09:00:00', @hs_2),
  (5, 1, '18:00:00', '09:00:00', @hs_2),
  (6, 0, NULL, NULL, @hs_2);

-- Horario semana 3: L-V 09:00:00-18:00:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana C', '2026-09-14', @emp_jav);
SET @hs_3 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '18:00:00', '09:00:00', @hs_3),
  (2, 1, '18:00:00', '09:00:00', @hs_3),
  (3, 1, '18:00:00', '09:00:00', @hs_3),
  (4, 1, '18:00:00', '09:00:00', @hs_3),
  (5, 1, '18:00:00', '09:00:00', @hs_3),
  (6, 0, NULL, NULL, @hs_3);

-- Horario semana 4: L-V 09:00:00-18:00:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana D', '2026-09-21', @emp_jav);
SET @hs_4 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '18:00:00', '09:00:00', @hs_4),
  (2, 1, '18:00:00', '09:00:00', @hs_4),
  (3, 1, '18:00:00', '09:00:00', @hs_4),
  (4, 1, '18:00:00', '09:00:00', @hs_4),
  (5, 1, '18:00:00', '09:00:00', @hs_4),
  (6, 0, NULL, NULL, @hs_4);

-- Asistencias (un registro por día hábil del 1 al 22 de septiembre):
--   1 falta injustificada: 09/09/2026
--   licencia médica (16/09 al 16/09): sin marcas, complete=1
--   día incompleto: 22/09/2026 (sin salida, next_step=SALIDA)
INSERT INTO asistencias
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-01','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-02','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-03','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-04','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-07','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-08','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (1,0,NULL,NULL,0,NULL,NULL,0,1,'2026-09-09',NULL,NULL,0,'ENTRADA',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-10','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-11','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-14','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-15','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,NULL,NULL,0,NULL,NULL,0,1,'2026-09-16',NULL,NULL,1,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-17','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-18','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-21','09:00:00','18:00:00',0,'COMPLETE',@emp_jav),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,0,'2026-09-22','09:00:00',NULL,0,'SALIDA',@emp_jav);

INSERT INTO licencias_medicas (employee_id, start_date, end_date, motivo, employer_pays)
VALUES (@emp_jav, '2026-09-16', '2026-09-16', 'Gripe', b'1');

-- ======================================================================
-- Sebastián Vargas — RUT 19345678-2 — código de marcaje 7915
-- ======================================================================

INSERT INTO empleados
  (afp_fund, base_salary, dependents, email, enabled, first_name,
   health_system, hire_date, last_name, pension_system, phone,
   rol_economico, rut, code)
VALUES
  ('CAPITAL', 1340000.00, 2, 'sebastian.vargas@empresa.cl', 1, 'Sebastián',
   'ISAPRE', '2021-02-15', 'Vargas', 'AFP', '965554433', '9', '19345678-2', '7915');

SELECT id INTO @emp_seb FROM empleados WHERE rut = '19345678-2';

-- Horario semana 1: L-V 08:30:00-17:30:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', @emp_seb);
SET @hs_1 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '17:30:00', '08:30:00', @hs_1),
  (2, 1, '17:30:00', '08:30:00', @hs_1),
  (3, 1, '17:30:00', '08:30:00', @hs_1),
  (4, 1, '17:30:00', '08:30:00', @hs_1),
  (5, 1, '17:30:00', '08:30:00', @hs_1),
  (6, 0, NULL, NULL, @hs_1);

-- Horario semana 2: L-V 08:30:00-17:30:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana B', '2026-09-07', @emp_seb);
SET @hs_2 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '17:30:00', '08:30:00', @hs_2),
  (2, 1, '17:30:00', '08:30:00', @hs_2),
  (3, 1, '17:30:00', '08:30:00', @hs_2),
  (4, 1, '17:30:00', '08:30:00', @hs_2),
  (5, 1, '17:30:00', '08:30:00', @hs_2),
  (6, 0, NULL, NULL, @hs_2);

-- Horario semana 3: L-V 08:30:00-17:30:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana C', '2026-09-14', @emp_seb);
SET @hs_3 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '17:30:00', '08:30:00', @hs_3),
  (2, 1, '17:30:00', '08:30:00', @hs_3),
  (3, 1, '17:30:00', '08:30:00', @hs_3),
  (4, 1, '17:30:00', '08:30:00', @hs_3),
  (5, 1, '17:30:00', '08:30:00', @hs_3),
  (6, 0, NULL, NULL, @hs_3);

-- Horario semana 4: L-V 08:30:00-17:30:00 (sábado no trabaja)
INSERT INTO horarios_semanales (label, start_date, employee_id)
VALUES ('Semana D', '2026-09-21', @emp_seb);
SET @hs_4 = LAST_INSERT_ID();
INSERT INTO dias_horario_semanal (day_number, worked, end_time, start_time, schedule_id) VALUES
  (1, 1, '17:30:00', '08:30:00', @hs_4),
  (2, 1, '17:30:00', '08:30:00', @hs_4),
  (3, 1, '17:30:00', '08:30:00', @hs_4),
  (4, 1, '17:30:00', '08:30:00', @hs_4),
  (5, 1, '17:30:00', '08:30:00', @hs_4),
  (6, 0, NULL, NULL, @hs_4);

-- Asistencias (un registro por día hábil del 1 al 22 de septiembre):
--   1 falta injustificada: 03/09/2026
--   licencia médica (10/09 al 11/09): sin marcas, complete=1
--   día incompleto: 22/09/2026 (sin salida, next_step=SALIDA)
INSERT INTO asistencias
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-01','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-02','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (1,0,NULL,NULL,0,NULL,NULL,0,1,'2026-09-03',NULL,NULL,0,'ENTRADA',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-04','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-07','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-08','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-09','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,NULL,NULL,0,NULL,NULL,0,1,'2026-09-10',NULL,NULL,1,'COMPLETE',@emp_seb),
  (0,0,NULL,NULL,0,NULL,NULL,0,1,'2026-09-11',NULL,NULL,1,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-14','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-15','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-16','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-17','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-18','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-21','08:30:00','17:30:00',0,'COMPLETE',@emp_seb),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,0,'2026-09-22','08:30:00',NULL,0,'SALIDA',@emp_seb);

INSERT INTO licencias_medicas (employee_id, start_date, end_date, motivo, employer_pays)
VALUES (@emp_seb, '2026-09-10', '2026-09-11', 'Gripe', b'1');

-- ======================================================================
-- VERIFICACIÓN
-- ======================================================================
SELECT e.first_name, e.last_name, e.rut, e.code,
       COUNT(a.id) AS dias_marcados,
       SUM(a.complete = b'1') AS completos,
       SUM(a.absence = b'1') AS ausencias,
       SUM(a.medical_license = b'1') AS licencias,
       SUM(a.complete = b'0') AS incompletos
FROM empleados e
LEFT JOIN asistencias a ON a.employee_id = e.id
GROUP BY e.id, e.first_name, e.last_name, e.rut, e.code
ORDER BY e.id;

SELECT 'Datos cargados correctamente' AS resultado;
