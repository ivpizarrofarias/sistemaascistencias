-- ========================================================================
-- INYECCIÓN DE DATOS DE EJEMPLO: EMPLEADOS, HORARIOS Y ASISTENCIAS
-- Base de datos: sistema_asis
-- Ejecutar: mysql -uroot -p'Admin_1983' sistema_asis < insertar_datos.sql
-- ========================================================================

-- ------------------------------------------------------------------------
-- 1) EMPLEADOS (si su RUT aún no existe)
-- ------------------------------------------------------------------------
INSERT INTO employees
  (afp_fund, base_salary, dependents, email, enabled, first_name,
   health_system, hire_date, last_name, pension_system, phone,
   rol_economico, rut, code)
VALUES
  ('HABITAT',     900000.00, 1, 'maria.gonzalez@empresa.cl', 1, 'María',
   'FONASA', '2023-03-15', 'González',    'AFP', '912345678', '9', '11223344-1', '9031MARI'),
  ('CAPITAL',     1200000.00, 2, 'carlos.reyes@empresa.cl',  1, 'Carlos',
   'ISAPRE', '2021-08-02', 'Reyes',       'AFP', '987654321', '9', '13456789-2', '9023CARL'),
  ('PROVIDA',     750000.00, 0, 'analucia.soto@empresa.cl',  1, 'Ana Lucía',
   'FONASA', '2022-11-20', 'Soto',        'AFP', '955512312', '9', '14567890-3', '9012ANAL')
ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id);

-- Los IDs de los empleados recién insertados quedan en @emp1, @emp2, @emp3.
-- (Se resuelven por RUT para que funcione si ya existían.)
SELECT id INTO @maria FROM employees WHERE rut = '11223344-1';
SELECT id INTO @carlos FROM employees WHERE rut = '13456789-2';
SELECT id INTO @ana    FROM employees WHERE rut = '14567890-3';

-- ------------------------------------------------------------------------
-- 2) HORARIOS SEMANALES (semana del 12/10/2026) + DÍAS
--    Usamos horas representativas L-V 09:00-18:00, Sábado 09:00-13:00 (opcional)
-- ------------------------------------------------------------------------
-- María
INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', @maria);
SET @sched_maria = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_maria),
  (2, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_maria),
  (3, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_maria),
  (4, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_maria),
  (5, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_maria),
  (6, '13:00:00', '09:00:00', 1, '13:00:00', '09:00:00', @sched_maria);

-- Carlos
INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', @carlos);
SET @sched_carlos = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '18:30:00', '08:30:00', 1, '18:30:00', '08:30:00', @sched_carlos),
  (2, '18:30:00', '08:30:00', 1, '18:30:00', '08:30:00', @sched_carlos),
  (3, '18:30:00', '08:30:00', 1, '18:30:00', '08:30:00', @sched_carlos),
  (4, '18:30:00', '08:30:00', 1, '18:30:00', '08:30:00', @sched_carlos),
  (5, '18:30:00', '08:30:00', 1, '18:30:00', '08:30:00', @sched_carlos);

-- Ana Lucía
INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', @ana);
SET @sched_ana = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '17:00:00', '09:30:00', 1, '17:00:00', '09:30:00', @sched_ana),
  (2, '17:00:00', '09:30:00', 1, '17:00:00', '09:30:00', @sched_ana),
  (3, '17:00:00', '09:30:00', 1, '17:00:00', '09:30:00', @sched_ana),
  (4, '17:00:00', '09:30:00', 1, '17:00:00', '09:30:00', @sched_ana),
  (5, '17:00:00', '09:30:00', 1, '17:00:00', '09:30:00', @sched_ana),
  (6, '13:00:00', '09:30:00', 1, '13:00:00', '09:30:00', @sched_ana);

-- Empleados existentes (Test = id 1, Nadia = id 4, Iván = id 5)
INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', 1);
SET @sched_1 = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_1),
  (2, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_1),
  (3, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_1),
  (4, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_1),
  (5, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_1);

INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', 4);
SET @sched_4 = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_4),
  (2, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_4),
  (3, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_4),
  (4, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_4),
  (5, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_4);

INSERT INTO weekly_schedules (label, start_date, employee_id)
VALUES ('Semana A', '2026-08-31', 5);
SET @sched_5 = LAST_INSERT_ID();
INSERT INTO weekly_schedule_days (day_number, end, `start`, worked, end_time, start_time, schedule_id) VALUES
  (1, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_5),
  (2, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_5),
  (3, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_5),
  (4, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_5),
  (5, '18:00:00', '09:00:00', 1, '18:00:00', '09:00:00', @sched_5);

-- ------------------------------------------------------------------------
-- 3) ASISTENCIAS de ejemplo: última semana (L-V) para cada empleado
-- ------------------------------------------------------------------------
-- Formato bit: absence, admin_corrected, complete, medical_license
-- Empleado María (@maria)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-08-31','09:03:00','18:02:00',0,'COMPLETE',@maria),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-01','09:01:00','18:05:00',0,'COMPLETE',@maria),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-02','08:58:00','18:00:00',0,'COMPLETE',@maria),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-03','09:05:00','18:10:00',0,'COMPLETE',@maria),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-04','09:00:00','18:00:00',0,'COMPLETE',@maria);

-- Empleado Carlos (@carlos)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-08-31','08:32:00','18:35:00',0,'COMPLETE',@carlos),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-01','08:29:00','18:30:00',0,'COMPLETE',@carlos),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,0,'2026-09-02','08:31:00',NULL,0,'SALIDA',@carlos),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-03','08:33:00','18:32:00',0,'COMPLETE',@carlos),
  (0,0,'13:30:00',NULL,1,'12:30:00',NULL,1,1,'2026-09-04','08:27:00','18:28:00',0,'COMPLETE',@carlos);

-- Empleada Ana Lucía (@ana)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:15:00',NULL,1,'13:15:00',NULL,1,1,'2026-08-31','09:32:00','17:02:00',0,'COMPLETE',@ana),
  (0,0,'14:15:00',NULL,1,'13:15:00',NULL,1,1,'2026-09-01','09:28:00','17:00:00',0,'COMPLETE',@ana),
  (0,0,'14:15:00',NULL,1,'13:15:00',NULL,1,1,'2026-09-02','09:35:00','17:05:00',0,'COMPLETE',@ana),
  (0,0,'14:15:00',NULL,1,'13:15:00',NULL,1,1,'2026-09-03','09:30:00','17:03:00',0,'COMPLETE',@ana),
  (0,0,'14:15:00',NULL,1,'13:15:00',NULL,1,0,'2026-09-04','09:31:00',NULL,0,'SALIDA',@ana);

-- Empleados existentes: Test (1)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-08-31','09:00:00','18:00:00',0,'COMPLETE',1),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-01','09:02:00','18:01:00',0,'COMPLETE',1),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-02','08:59:00','18:03:00',0,'COMPLETE',1),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-03','09:01:00','18:00:00',0,'COMPLETE',1),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-04','09:04:00','18:02:00',0,'COMPLETE',1);

-- Nadia (4)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-08-31','09:00:00','18:00:00',0,'COMPLETE',4),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-01','09:03:00','18:04:00',0,'COMPLETE',4),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-02','08:57:00','18:00:00',0,'COMPLETE',4),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-03','09:02:00','18:03:00',0,'COMPLETE',4),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-04','09:00:00','18:01:00',0,'COMPLETE',4);

-- Iván (5)
INSERT INTO attendances
  (absence, admin_corrected, colacion_entrada1, colacion_entrada2, colacion_entrada_count,
   colacion_salida1, colacion_salida2, colacion_salida_count, complete, date,
   entry_time, exit_time, medical_license, next_step, employee_id) VALUES
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-08-31','09:00:00','18:00:00',0,'COMPLETE',5),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-01','09:05:00','18:06:00',0,'COMPLETE',5),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-02','08:58:00','18:00:00',0,'COMPLETE',5),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-03','09:03:00','18:02:00',0,'COMPLETE',5),
  (0,0,'14:00:00',NULL,1,'13:00:00',NULL,1,1,'2026-09-04','09:01:00','18:03:00',0,'COMPLETE',5);

SELECT 'Datos insertados correctamente' AS resultado;
