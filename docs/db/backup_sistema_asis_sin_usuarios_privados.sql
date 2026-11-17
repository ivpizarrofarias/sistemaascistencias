-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- Host: localhost    Database: sistema_asis
-- ------------------------------------------------------
-- Server version	8.4.11-0ubuntu0.26.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `sistema_asis`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `sistema_asis` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `sistema_asis`;

--
-- Table structure for table `anticipos`
--

DROP TABLE IF EXISTS `anticipos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anticipos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) NOT NULL,
  `date` date NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK78463n1gen45q4ha3y2itu5r0` (`employee_id`),
  CONSTRAINT `FK78463n1gen45q4ha3y2itu5r0` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anticipos`
--

LOCK TABLES `anticipos` WRITE;
/*!40000 ALTER TABLE `anticipos` DISABLE KEYS */;
INSERT INTO `anticipos` VALUES (1,150000.00,'2026-11-05','Anticipo de sueldo quincena',1),(2,100000.00,'2026-11-07','Anticipo por gasto personal',2),(3,120000.00,'2026-11-10','Anticipo de sueldo',3),(4,80000.00,'2026-11-08','Anticipo quincenal',5),(5,200000.00,'2026-11-12','Anticipo para emergencia familiar',6),(6,90000.00,'2026-11-06','Anticipo de sueldo',8),(7,110000.00,'2026-11-11','Anticipo solicitado',10);
/*!40000 ALTER TABLE `anticipos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asistencias`
--

DROP TABLE IF EXISTS `asistencias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asistencias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `absence` bit(1) NOT NULL,
  `admin_corrected` bit(1) NOT NULL,
  `colacion_entrada1` time DEFAULT NULL,
  `colacion_entrada2` time DEFAULT NULL,
  `colacion_entrada_count` int NOT NULL,
  `colacion_salida1` time DEFAULT NULL,
  `colacion_salida2` time DEFAULT NULL,
  `colacion_salida_count` int NOT NULL,
  `complete` bit(1) NOT NULL,
  `date` date NOT NULL,
  `entry_time` time DEFAULT NULL,
  `exit_time` time DEFAULT NULL,
  `medical_license` bit(1) NOT NULL,
  `next_step` varchar(20) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKch065bjh8d21r7uudqq40olwe` (`employee_id`),
  CONSTRAINT `FKch065bjh8d21r7uudqq40olwe` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asistencias`
--

LOCK TABLES `asistencias` WRITE;
/*!40000 ALTER TABLE `asistencias` DISABLE KEYS */;
INSERT INTO `asistencias` VALUES (1,_binary '\0',_binary '\0','13:45:00',NULL,1,'13:00:00',NULL,1,_binary '','2026-11-16','08:58:00','18:05:00',_binary '\0','COMPLETE',1),(2,_binary '\0',_binary '\0','13:55:00',NULL,1,'13:10:00',NULL,1,_binary '','2026-11-16','09:05:00','18:00:00',_binary '\0','COMPLETE',2),(3,_binary '\0',_binary '\0','14:00:00',NULL,1,'13:00:00',NULL,1,_binary '','2026-11-16','09:00:00','18:10:00',_binary '\0','COMPLETE',3),(4,_binary '\0',_binary '\0',NULL,NULL,0,NULL,NULL,0,_binary '\0','2026-11-16','09:10:00',NULL,_binary '\0','COLACION_SALIDA',4),(5,_binary '',_binary '\0',NULL,NULL,0,NULL,NULL,0,_binary '\0','2026-11-16',NULL,NULL,_binary '\0','ENTRADA',5),(6,_binary '\0',_binary '\0','13:40:00',NULL,1,'13:00:00',NULL,1,_binary '','2026-11-16','08:55:00','17:58:00',_binary '\0','COMPLETE',6),(7,_binary '\0',_binary '\0','13:50:00',NULL,1,'13:05:00',NULL,1,_binary '','2026-11-16','09:02:00','18:03:00',_binary '\0','COMPLETE',7),(8,_binary '\0',_binary '\0','13:45:00',NULL,1,'13:00:00',NULL,1,_binary '','2026-11-16','08:57:00','18:07:00',_binary '\0','COMPLETE',8),(9,_binary '\0',_binary '\0','14:00:00',NULL,1,'13:15:00',NULL,1,_binary '','2026-11-16','09:08:00','18:00:00',_binary '\0','COMPLETE',9),(10,_binary '\0',_binary '\0','13:40:00',NULL,1,'13:00:00',NULL,1,_binary '','2026-11-16','09:00:00','18:12:00',_binary '\0','COMPLETE',10),(11,_binary '\0',_binary '\0','13:55:00',NULL,1,'13:10:00',NULL,1,_binary '','2026-11-16','08:59:00','18:01:00',_binary '\0','COMPLETE',11),(12,_binary '\0',_binary '\0','11:13:16',NULL,1,'11:13:12',NULL,1,_binary '','2026-11-17','11:12:34','11:13:18',_binary '\0',NULL,1);
/*!40000 ALTER TABLE `asistencias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dias_horario_semanal`
--

DROP TABLE IF EXISTS `dias_horario_semanal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dias_horario_semanal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `day_number` int NOT NULL,
  `end_time` time DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `worked` bit(1) NOT NULL,
  `schedule_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmu6aomnq9pgr52cc0p3kgdvxg` (`schedule_id`,`day_number`),
  CONSTRAINT `FKepy202dujg1ko3mcnmufh762k` FOREIGN KEY (`schedule_id`) REFERENCES `horarios_semanales` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dias_horario_semanal`
--

LOCK TABLES `dias_horario_semanal` WRITE;
/*!40000 ALTER TABLE `dias_horario_semanal` DISABLE KEYS */;
INSERT INTO `dias_horario_semanal` VALUES (1,1,'16:30:00','07:30:00',_binary '',1),(2,2,'16:30:00','07:30:00',_binary '',1),(3,3,'16:30:00','07:30:00',_binary '',1),(4,4,'16:30:00','07:30:00',_binary '',1),(5,5,'16:30:00','07:30:00',_binary '',1),(6,6,'16:00:00','08:00:00',_binary '',1),(7,1,'18:00:00','09:00:00',_binary '',2),(8,2,'18:00:00','09:00:00',_binary '',2),(9,3,'18:00:00','09:00:00',_binary '',2),(10,4,'18:00:00','09:00:00',_binary '',2),(11,5,'18:00:00','09:00:00',_binary '',2),(12,6,NULL,NULL,_binary '\0',2),(13,1,'18:00:00','09:00:00',_binary '',3),(14,2,'18:00:00','09:00:00',_binary '',3),(15,3,'18:00:00','09:00:00',_binary '',3),(16,4,'18:00:00','09:00:00',_binary '',3),(17,5,'18:00:00','09:00:00',_binary '',3),(18,6,NULL,NULL,_binary '\0',3),(19,1,'18:00:00','09:00:00',_binary '',4),(20,2,'18:00:00','09:00:00',_binary '',4),(21,3,'18:00:00','09:00:00',_binary '',4),(22,4,'18:00:00','09:00:00',_binary '',4),(23,5,'18:00:00','09:00:00',_binary '',4),(24,6,NULL,NULL,_binary '\0',4),(25,1,'18:00:00','09:00:00',_binary '',5),(26,2,'18:00:00','09:00:00',_binary '',5),(27,3,'18:00:00','09:00:00',_binary '',5),(28,4,'18:00:00','09:00:00',_binary '',5),(29,5,'18:00:00','09:00:00',_binary '',5),(30,6,NULL,NULL,_binary '\0',5),(31,1,'18:00:00','09:00:00',_binary '',6),(32,2,'18:00:00','09:00:00',_binary '',6),(33,3,'18:00:00','09:00:00',_binary '',6),(34,4,'18:00:00','09:00:00',_binary '',6),(35,5,'18:00:00','09:00:00',_binary '',6),(36,6,NULL,NULL,_binary '\0',6),(37,1,'18:00:00','09:00:00',_binary '',7),(38,2,'18:00:00','09:00:00',_binary '',7),(39,3,'18:00:00','09:00:00',_binary '',7),(40,4,'18:00:00','09:00:00',_binary '',7),(41,5,'18:00:00','09:00:00',_binary '',7),(42,6,NULL,NULL,_binary '\0',7),(43,1,'18:00:00','09:00:00',_binary '',8),(44,2,'18:00:00','09:00:00',_binary '',8),(45,3,'18:00:00','09:00:00',_binary '',8),(46,4,'18:00:00','09:00:00',_binary '',8),(47,5,'18:00:00','09:00:00',_binary '',8),(48,6,NULL,NULL,_binary '\0',8),(49,1,'18:00:00','09:00:00',_binary '',9),(50,2,'18:00:00','09:00:00',_binary '',9),(51,3,'18:00:00','09:00:00',_binary '',9),(52,4,'18:00:00','09:00:00',_binary '',9),(53,5,'18:00:00','09:00:00',_binary '',9),(54,6,NULL,NULL,_binary '\0',9),(55,1,'18:00:00','09:00:00',_binary '',10),(56,2,'18:00:00','09:00:00',_binary '',10),(57,3,'18:00:00','09:00:00',_binary '',10),(58,4,'18:00:00','09:00:00',_binary '',10),(59,5,'18:00:00','09:00:00',_binary '',10),(60,6,NULL,NULL,_binary '\0',10),(61,1,'18:00:00','09:00:00',_binary '',11),(62,2,'18:00:00','09:00:00',_binary '',11),(63,3,'18:00:00','09:00:00',_binary '',11),(64,4,'18:00:00','09:00:00',_binary '',11),(65,5,'18:00:00','09:00:00',_binary '',11),(66,6,NULL,NULL,_binary '\0',11);
/*!40000 ALTER TABLE `dias_horario_semanal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empleados`
--

DROP TABLE IF EXISTS `empleados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empleados` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `afp_fund` varchar(20) NOT NULL,
  `base_salary` decimal(15,2) NOT NULL,
  `code` varchar(20) DEFAULT NULL,
  `dependents` int NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `health_system` varchar(20) NOT NULL,
  `hire_date` date NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `pension_system` varchar(10) NOT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `rol_economico` varchar(255) NOT NULL,
  `rut` varchar(12) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl5r0paa0ls09d9nntxidonbnt` (`rut`),
  UNIQUE KEY `UKpmtm2docajq2gt2x0uxf2hcst` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empleados`
--

LOCK TABLES `empleados` WRITE;
/*!40000 ALTER TABLE `empleados` DISABLE KEYS */;
INSERT INTO `empleados` VALUES (1,'AFP MODELO',550000.00,'3238',2,NULL,_binary '','Abel Patricio','FONASA','2025-02-28','Ponce Carmona','AFP',NULL,'9','15779326-8'),(2,'AFP PROVIDA',550000.00,'6976',0,NULL,_binary '','Camila','FONASA','2024-10-16','Ponce Varas','AFP',NULL,'9','7895226-1'),(3,'AFP HABITAT',850000.00,'6977',1,'maria.gonzalez@example.com',_binary '','María José','FONASA','2024-03-01','González Rojas','AFP','+56911112222','9','12345678-5'),(4,'AFP PROVIDA',920000.00,'6978',0,'juan.munoz@example.com',_binary '','Juan Pablo','ISAPRE','2023-11-15','Muñoz Soto','AFP','+56922223333','9','11222333-9'),(5,'AFP CUPRUM',780000.00,'6979',2,'camila.rojas@example.com',_binary '','Camila Andrea','FONASA','2025-01-10','Rojas Díaz','AFP','+56933334444','9','15678901-1'),(6,'AFP MODELO',990000.00,'6980',1,'pedro.lopez@example.com',_binary '','Pedro Ignacio','ISAPRE','2024-07-20','López Herrera','AFP','+56944445555','9','20123456-5'),(7,'AFP UNO',870000.00,'6981',0,'valentina.contreras@example.com',_binary '','Valentina Paz','FONASA','2024-09-02','Contreras Silva','AFP','+56955556666','9','13456789-9'),(8,'AFP PLANVITAL',910000.00,'6982',1,'diego.fernandez@example.com',_binary '','Diego Andrés','ISAPRE','2025-02-18','Fernández Torres','AFP','+56966667777','9','14567890-0'),(9,'AFP HABITAT',760000.00,'6983',2,'francisca.vega@example.com',_binary '','Francisca Belén','FONASA','2024-05-06','Vega Morales','AFP','+56977778888','9','17234567-0'),(10,'AFP PROVIDA',940000.00,'6984',0,'matias.castro@example.com',_binary '','Matías Alonso','ISAPRE','2023-08-14','Castro Peña','AFP','+56988889999','9','18345678-4'),(11,'AFP MODELO',830000.00,'6985',1,'javiera.soto@example.com',_binary '','Javiera Antonia','FONASA','2025-03-03','Soto Ramírez','AFP','+56999990000','9','19456789-8');
/*!40000 ALTER TABLE `empleados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horarios_semanales`
--

DROP TABLE IF EXISTS `horarios_semanales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horarios_semanales` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(100) DEFAULT NULL,
  `start_date` date NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKehghh0a6wi3s0tr2odmp1c5om` (`employee_id`),
  CONSTRAINT `FKehghh0a6wi3s0tr2odmp1c5om` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horarios_semanales`
--

LOCK TABLES `horarios_semanales` WRITE;
/*!40000 ALTER TABLE `horarios_semanales` DISABLE KEYS */;
INSERT INTO `horarios_semanales` VALUES (1,'Semana del 2026-11-16','2026-11-16',1),(2,'Semana del 2026-11-16','2026-11-16',2),(3,'Semana del 2026-11-16','2026-11-16',3),(4,'Semana del 2026-11-16','2026-11-16',4),(5,'Semana del 2026-11-16','2026-11-16',5),(6,'Semana del 2026-11-16','2026-11-16',6),(7,'Semana del 2026-11-16','2026-11-16',7),(8,'Semana del 2026-11-16','2026-11-16',8),(9,'Semana del 2026-11-16','2026-11-16',9),(10,'Semana del 2026-11-16','2026-11-16',10),(11,'Semana del 2026-11-16','2026-11-16',11);
/*!40000 ALTER TABLE `horarios_semanales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horas_extra`
--

DROP TABLE IF EXISTS `horas_extra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horas_extra` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `horas` decimal(10,2) NOT NULL,
  `monto` decimal(15,2) NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlc40yu44iui1jujlj2qu582lj` (`employee_id`),
  CONSTRAINT `FKlc40yu44iui1jujlj2qu582lj` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horas_extra`
--

LOCK TABLES `horas_extra` WRITE;
/*!40000 ALTER TABLE `horas_extra` DISABLE KEYS */;
INSERT INTO `horas_extra` VALUES (1,'2026-11-12',2.00,15000.00,1),(2,'2026-11-13',1.50,12000.00,2),(3,'2026-11-14',3.00,24000.00,3),(4,'2026-11-11',1.00,8000.00,4),(5,'2026-11-15',2.50,20000.00,6),(6,'2026-11-12',1.00,9000.00,7),(7,'2026-11-13',2.00,17000.00,10),(8,'2026-11-14',1.50,13000.00,11);
/*!40000 ALTER TABLE `horas_extra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `licencias_medicas`
--

DROP TABLE IF EXISTS `licencias_medicas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `licencias_medicas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_date` date NOT NULL,
  `motivo` varchar(200) NOT NULL,
  `start_date` date NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhqe5l0u6cu5vs90o1r67tha1f` (`employee_id`),
  CONSTRAINT `FKhqe5l0u6cu5vs90o1r67tha1f` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `licencias_medicas`
--

LOCK TABLES `licencias_medicas` WRITE;
/*!40000 ALTER TABLE `licencias_medicas` DISABLE KEYS */;
INSERT INTO `licencias_medicas` VALUES (1,'2026-11-14','Reposo médico por cuadro viral','2026-11-10',5),(2,'2026-11-05','Licencia por procedimiento médico','2026-11-03',9),(3,'2026-10-22','Reposo por lumbalgia','2026-10-20',2);
/*!40000 ALTER TABLE `licencias_medicas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `liquidacion_detalle`
--

DROP TABLE IF EXISTS `liquidacion_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `liquidacion_detalle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(100) NOT NULL,
  `haber` bit(1) NOT NULL,
  `monto` decimal(15,2) NOT NULL,
  `liquidacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdn3xgdf5an01hvc93ep9fgrfb` (`liquidacion_id`),
  CONSTRAINT `FKdn3xgdf5an01hvc93ep9fgrfb` FOREIGN KEY (`liquidacion_id`) REFERENCES `liquidaciones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquidacion_detalle`
--

LOCK TABLES `liquidacion_detalle` WRITE;
/*!40000 ALTER TABLE `liquidacion_detalle` DISABLE KEYS */;
INSERT INTO `liquidacion_detalle` VALUES (104,'Sueldo base',_binary '',550000.00,12),(105,'Gratificación legal',_binary '',3929.00,12),(106,'Asignación familiar (2 cargas)',_binary '',45202.00,12),(107,'Bono de movilización',_binary '',30000.00,12),(108,'Horas extra (2 h)',_binary '',15000.00,12),(109,'AFP (AFP MODELO 10%)',_binary '\0',56893.00,12),(110,'Salud (7%)',_binary '\0',39825.00,12),(111,'Cesantía (trabajador)',_binary '\0',3414.00,12),(112,'Impuesto Único 2ª categoría',_binary '\0',0.00,12),(113,'Anticipo de sueldo',_binary '\0',150000.00,12),(114,'Descuento por permisos laborales',_binary '\0',6548.00,12),(115,'Sueldo base',_binary '',550000.00,13),(116,'Gratificación legal',_binary '',3929.00,13),(117,'Bono de movilización',_binary '',30000.00,13),(118,'Horas extra (1.5 h)',_binary '',12000.00,13),(119,'AFP (AFP PROVIDA 10%)',_binary '\0',56593.00,13),(120,'Salud (7%)',_binary '\0',39615.00,13),(121,'Cesantía (trabajador)',_binary '\0',3396.00,13),(122,'Impuesto Único 2ª categoría',_binary '\0',0.00,13),(123,'Anticipo de sueldo',_binary '\0',100000.00,13),(124,'Sueldo base',_binary '',850000.00,14),(125,'Gratificación legal',_binary '',6071.00,14),(126,'Asignación familiar (1 cargas)',_binary '',13870.00,14),(127,'Bono de movilización',_binary '',30000.00,14),(128,'Horas extra (3 h)',_binary '',24000.00,14),(129,'AFP (AFP HABITAT 10%)',_binary '\0',88007.00,14),(130,'Salud (7%)',_binary '\0',61605.00,14),(131,'Cesantía (trabajador)',_binary '\0',5280.00,14),(132,'Impuesto Único 2ª categoría',_binary '\0',0.00,14),(133,'Anticipo de sueldo',_binary '\0',120000.00,14),(134,'Sueldo base',_binary '',920000.00,15),(135,'Bono de movilización',_binary '',30000.00,15),(136,'Horas extra (1 h)',_binary '',8000.00,15),(137,'AFP (AFP PROVIDA 10%)',_binary '\0',92800.00,15),(138,'Salud (7%)',_binary '\0',64960.00,15),(139,'Cesantía (trabajador)',_binary '\0',5568.00,15),(140,'Impuesto Único 2ª categoría',_binary '\0',0.00,15),(141,'Sueldo base',_binary '',780000.00,16),(142,'Asignación familiar (2 cargas)',_binary '',27740.00,16),(143,'Bono de movilización',_binary '',30000.00,16),(144,'AFP (AFP CUPRUM 10%)',_binary '\0',78000.00,16),(145,'Salud (7%)',_binary '\0',54600.00,16),(146,'Cesantía (trabajador)',_binary '\0',4680.00,16),(147,'Impuesto Único 2ª categoría',_binary '\0',0.00,16),(148,'Descuento por inasistencia (1 días)',_binary '\0',37143.00,16),(149,'Descuento por licencia médica (5 días a cargo del trabajador)',_binary '\0',185714.00,16),(150,'Anticipo de sueldo',_binary '\0',80000.00,16),(151,'Sueldo base',_binary '',990000.00,17),(152,'Gratificación legal',_binary '',7071.00,17),(153,'Asignación familiar (1 cargas)',_binary '',4382.00,17),(154,'Bono de movilización',_binary '',30000.00,17),(155,'Horas extra (2.5 h)',_binary '',20000.00,17),(156,'AFP (AFP MODELO 10%)',_binary '\0',101707.00,17),(157,'Salud (7%)',_binary '\0',71195.00,17),(158,'Cesantía (trabajador)',_binary '\0',6102.00,17),(159,'Impuesto Único 2ª categoría',_binary '\0',0.00,17),(160,'Anticipo de sueldo',_binary '\0',200000.00,17),(161,'Descuento por permisos laborales',_binary '\0',47143.00,17),(162,'Sueldo base',_binary '',870000.00,18),(163,'Gratificación legal',_binary '',6214.00,18),(164,'Bono de movilización',_binary '',30000.00,18),(165,'Horas extra (1 h)',_binary '',9000.00,18),(166,'AFP (AFP UNO 10%)',_binary '\0',88521.00,18),(167,'Salud (7%)',_binary '\0',61965.00,18),(168,'Cesantía (trabajador)',_binary '\0',5311.00,18),(169,'Impuesto Único 2ª categoría',_binary '\0',0.00,18),(170,'Sueldo base',_binary '',910000.00,19),(171,'Gratificación legal',_binary '',6500.00,19),(172,'Asignación familiar (1 cargas)',_binary '',13870.00,19),(173,'Bono de movilización',_binary '',30000.00,19),(174,'AFP (AFP PLANVITAL 10%)',_binary '\0',91650.00,19),(175,'Salud (7%)',_binary '\0',64155.00,19),(176,'Cesantía (trabajador)',_binary '\0',5499.00,19),(177,'Impuesto Único 2ª categoría',_binary '\0',0.00,19),(178,'Anticipo de sueldo',_binary '\0',90000.00,19),(179,'Descuento por permisos laborales',_binary '\0',86667.00,19),(180,'Sueldo base',_binary '',760000.00,20),(181,'Gratificación legal',_binary '',5429.00,20),(182,'Asignación familiar (2 cargas)',_binary '',27740.00,20),(183,'Bono de movilización',_binary '',30000.00,20),(184,'AFP (AFP HABITAT 10%)',_binary '\0',76543.00,20),(185,'Salud (7%)',_binary '\0',53580.00,20),(186,'Cesantía (trabajador)',_binary '\0',4593.00,20),(187,'Impuesto Único 2ª categoría',_binary '\0',0.00,20),(188,'Descuento por licencia médica (3 días a cargo del trabajador)',_binary '\0',108571.00,20),(189,'Sueldo base',_binary '',940000.00,21),(190,'Gratificación legal',_binary '',6714.00,21),(191,'Bono de movilización',_binary '',30000.00,21),(192,'Horas extra (2 h)',_binary '',17000.00,21),(193,'AFP (AFP PROVIDA 10%)',_binary '\0',96371.00,21),(194,'Salud (7%)',_binary '\0',67460.00,21),(195,'Cesantía (trabajador)',_binary '\0',5782.00,21),(196,'Impuesto Único 2ª categoría',_binary '\0',0.00,21),(197,'Anticipo de sueldo',_binary '\0',110000.00,21),(198,'Sueldo base',_binary '',830000.00,22),(199,'Gratificación legal',_binary '',5929.00,22),(200,'Asignación familiar (1 cargas)',_binary '',13870.00,22),(201,'Bono de movilización',_binary '',30000.00,22),(202,'Horas extra (1.5 h)',_binary '',13000.00,22),(203,'AFP (AFP MODELO 10%)',_binary '\0',84893.00,22),(204,'Salud (7%)',_binary '\0',59425.00,22),(205,'Cesantía (trabajador)',_binary '\0',5094.00,22),(206,'Impuesto Único 2ª categoría',_binary '\0',0.00,22);
/*!40000 ALTER TABLE `liquidacion_detalle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `liquidaciones`
--

DROP TABLE IF EXISTS `liquidaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `liquidaciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `afp` decimal(15,2) NOT NULL,
  `anticipos` decimal(15,2) NOT NULL,
  `asignacion_familiar` decimal(15,2) NOT NULL,
  `base_imponible` decimal(15,2) NOT NULL,
  `bono_movilizacion` decimal(15,2) NOT NULL,
  `cesantia` decimal(15,2) NOT NULL,
  `descuento_faltas` decimal(15,2) NOT NULL,
  `descuento_horas_extras` decimal(15,2) NOT NULL,
  `descuento_licencia` decimal(15,2) NOT NULL,
  `descuento_licencia_dias` int NOT NULL,
  `dias_habiles` int NOT NULL,
  `dias_inasistencia` int NOT NULL,
  `dias_licencia` int NOT NULL,
  `dias_mes` int NOT NULL,
  `dias_trabajados` int NOT NULL,
  `gratificacion` decimal(15,2) NOT NULL,
  `horas_extras` decimal(10,2) NOT NULL,
  `impuesto_unico` decimal(15,2) NOT NULL,
  `liquidoapagar` decimal(15,2) NOT NULL,
  `monto_horas_extras` decimal(15,2) NOT NULL,
  `otros_descuentos` decimal(15,2) NOT NULL,
  `otros_haberes` decimal(15,2) NOT NULL,
  `pagada` bit(1) NOT NULL,
  `period_month` int NOT NULL,
  `period_year` int NOT NULL,
  `salud` decimal(15,2) NOT NULL,
  `sis` decimal(15,2) NOT NULL,
  `sueldo_base` decimal(15,2) NOT NULL,
  `total_descuentos` decimal(15,2) NOT NULL,
  `total_haberes` decimal(15,2) NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKav7l04i75y22vxupf17xoxghh` (`employee_id`),
  CONSTRAINT `FKav7l04i75y22vxupf17xoxghh` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquidaciones`
--

LOCK TABLES `liquidaciones` WRITE;
/*!40000 ALTER TABLE `liquidaciones` DISABLE KEYS */;
INSERT INTO `liquidaciones` VALUES (12,56893.00,150000.00,45202.00,0.00,30000.00,3414.00,0.00,0.00,0.00,0,21,0,0,30,1,3929.00,2.00,0.00,387451.00,15000.00,6548.00,0.00,_binary '\0',11,2026,39825.00,0.00,550000.00,256680.00,644131.00,1),(13,56593.00,100000.00,0.00,0.00,30000.00,3396.00,0.00,0.00,0.00,0,21,0,0,30,1,3929.00,1.50,0.00,396325.00,12000.00,0.00,0.00,_binary '\0',11,2026,39615.00,0.00,550000.00,199604.00,595929.00,2),(14,88007.00,120000.00,13870.00,0.00,30000.00,5280.00,0.00,0.00,0.00,0,21,0,0,30,1,6071.00,3.00,0.00,649049.00,24000.00,0.00,0.00,_binary '\0',11,2026,61605.00,0.00,850000.00,274892.00,923941.00,3),(15,92800.00,0.00,0.00,0.00,30000.00,5568.00,0.00,0.00,0.00,0,21,0,0,30,0,0.00,1.00,0.00,794672.00,8000.00,0.00,0.00,_binary '\0',11,2026,64960.00,0.00,920000.00,163328.00,958000.00,4),(16,78000.00,80000.00,27740.00,0.00,30000.00,4680.00,37143.00,0.00,185714.00,5,21,1,0,30,0,0.00,0.00,0.00,397603.00,0.00,0.00,0.00,_binary '\0',11,2026,54600.00,0.00,780000.00,440137.00,837740.00,5),(17,101707.00,200000.00,4382.00,0.00,30000.00,6102.00,0.00,0.00,0.00,0,21,0,0,30,1,7071.00,2.50,0.00,625306.00,20000.00,47143.00,0.00,_binary '\0',11,2026,71195.00,0.00,990000.00,426147.00,1051453.00,6),(18,88521.00,0.00,0.00,0.00,30000.00,5311.00,0.00,0.00,0.00,0,21,0,0,30,1,6214.00,1.00,0.00,759417.00,9000.00,0.00,0.00,_binary '\0',11,2026,61965.00,0.00,870000.00,155797.00,915214.00,7),(19,91650.00,90000.00,13870.00,0.00,30000.00,5499.00,0.00,0.00,0.00,0,21,0,0,30,1,6500.00,0.00,0.00,622399.00,0.00,86667.00,0.00,_binary '\0',11,2026,64155.00,0.00,910000.00,337971.00,960370.00,8),(20,76543.00,0.00,27740.00,0.00,30000.00,4593.00,0.00,0.00,108571.00,3,21,0,0,30,1,5429.00,0.00,0.00,579882.00,0.00,0.00,0.00,_binary '\0',11,2026,53580.00,0.00,760000.00,243287.00,823169.00,9),(21,96371.00,110000.00,0.00,0.00,30000.00,5782.00,0.00,0.00,0.00,0,21,0,0,30,1,6714.00,2.00,0.00,714101.00,17000.00,0.00,0.00,_binary '\0',11,2026,67460.00,0.00,940000.00,279613.00,993714.00,10),(22,84893.00,0.00,13870.00,0.00,30000.00,5094.00,0.00,0.00,0.00,0,21,0,0,30,1,5929.00,1.50,0.00,743387.00,13000.00,0.00,0.00,_binary '\0',11,2026,59425.00,0.00,830000.00,149412.00,892799.00,11);
/*!40000 ALTER TABLE `liquidaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parametros_sistema`
--

DROP TABLE IF EXISTS `parametros_sistema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parametros_sistema` (
  `param_key` varchar(50) NOT NULL,
  `description` varchar(200) NOT NULL,
  `param_value` decimal(20,4) NOT NULL,
  PRIMARY KEY (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parametros_sistema`
--

LOCK TABLES `parametros_sistema` WRITE;
/*!40000 ALTER TABLE `parametros_sistema` DISABLE KEYS */;
INSERT INTO `parametros_sistema` VALUES ('asignacion_familiar_tramo1','Asignación familiar tramo 1 (por carga, en CLP)',21569.0000),('bono_movilizacion','Bono de movilización obligatorio (CLP)',30000.0000),('cesantia_empleado_pct','Seguro de Cesantía - parte del trabajador (0.6%)',0.0060),('gratificacion_pct','Porcentaje de gratificación legal (15% o 4.25 1/M)',0.1500),('horas_extra_mult','Multiplicador de las horas extra sobre el valor hora',1.5000),('uf','Valor de la UF en pesos',38000.0000),('utm','Valor de la UTM en pesos (para Impuesto Único)',67000.0000);
/*!40000 ALTER TABLE `parametros_sistema` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permisos_laborales`
--

DROP TABLE IF EXISTS `permisos_laborales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permisos_laborales` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `dias` int NOT NULL,
  `dias_devueltos` int NOT NULL,
  `estado` enum('APROBADO','PENDIENTE','RECHAZADO') NOT NULL,
  `fecha_solicitud` date DEFAULT NULL,
  `fecha_validacion` date DEFAULT NULL,
  `forma_compensacion` enum('DESCUENTO','DEVOLVER') NOT NULL,
  `horas` decimal(38,2) NOT NULL,
  `horas_devueltas` decimal(38,2) NOT NULL,
  `motivo` varchar(300) NOT NULL,
  `tipo` enum('POR_DIA','POR_HORA') NOT NULL,
  `validado_por` varchar(100) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqv2s1da3forkqdk1o9b44c374` (`employee_id`),
  CONSTRAINT `FKqv2s1da3forkqdk1o9b44c374` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permisos_laborales`
--

LOCK TABLES `permisos_laborales` WRITE;
/*!40000 ALTER TABLE `permisos_laborales` DISABLE KEYS */;
INSERT INTO `permisos_laborales` VALUES (1,'2026-11-18',0,0,'APROBADO','2026-11-12','2026-11-13','DESCUENTO',2.00,0.00,'Trámite personal','POR_HORA','Iván Pizarro',1),(2,'2026-11-20',1,0,'PENDIENTE','2026-11-15',NULL,'DEVOLVER',0.00,0.00,'Asunto familiar','POR_DIA',NULL,3),(3,'2026-11-19',0,0,'RECHAZADO','2026-11-14','2026-11-14','DESCUENTO',3.00,0.00,'Cita médica no programada','POR_HORA','Iván Pizarro',4),(4,'2026-11-25',1,0,'APROBADO','2026-11-16','2026-11-16','DEVOLVER',0.00,0.00,'Diligencia personal','POR_DIA','Iván Pizarro',6),(5,'2026-11-21',0,0,'PENDIENTE','2026-11-16',NULL,'DESCUENTO',1.50,0.00,'Salida por trámite bancario','POR_HORA',NULL,7),(6,'2026-11-27',2,0,'APROBADO','2026-11-10','2026-11-11','DESCUENTO',0.00,0.00,'Viaje familiar','POR_DIA','Iván Pizarro',8);
/*!40000 ALTER TABLE `permisos_laborales` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-17 11:42:03
-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- Host: localhost    Database: sistema_asis
-- ------------------------------------------------------
-- Server version	8.4.11-0ubuntu0.26.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `username` varchar(60) NOT NULL,
  `employee_id` bigint DEFAULT NULL,
  `foto_base64` longtext,
  `full_name` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm2dvbwfge291euvmk6vkkocao` (`username`),
  UNIQUE KEY `UK1jvg1j0joinh59akjnawkpkt3` (`employee_id`),
  CONSTRAINT `FKibye5ekq5cvoo60jw3mqb8xgq` FOREIGN KEY (`employee_id`) REFERENCES `empleados` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-17 11:42:03
INSERT INTO `usuarios` VALUES (4,_binary '','$2a$10$3E9ryabfjUmFNSOsegvfNufCiMDOgiuyh7euawctIPQQyuSg42.t.','ADMIN','admin',NULL,NULL,NULL);
