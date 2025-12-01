-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: websitedb
-- ------------------------------------------------------
-- Server version  8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
 /*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
 /*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 /*!50503 SET NAMES utf8 */;
 /*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
 /*!40103 SET TIME_ZONE='+00:00' */;
 /*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
 /*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
 /*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
 /*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `eventlog`
--

DROP TABLE IF EXISTS `eventlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 /*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eventlog` (
  `event_id`      int NOT NULL AUTO_INCREMENT,
  `user_id`       int NOT NULL,
  `table_name`    varchar(50) NOT NULL,
  `action_type`   enum('INSERT','UPDATE','DELETE') NOT NULL,
  `account_id`    int DEFAULT NULL,                -- NEW: direct link for account events
  `record_before` json DEFAULT NULL,
  `record_after`  json DEFAULT NULL,
  `event_time`    timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`event_id`),
  KEY `ix_eventlog_table_time` (`table_name`,`event_time`),
  KEY `ix_eventlog_account_time` (`account_id`,`event_time`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `eventlog_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `eventlog_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Flat view for easy "before/after" reading specifically for Accounts
--

DROP VIEW IF EXISTS `v_accounts_eventlog`;
CREATE VIEW `v_accounts_eventlog` AS
SELECT
  e.event_id,
  e.account_id,
  e.user_id AS changed_by,
  e.event_time AS changed_at,
  e.action_type,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_before, '$.account_name'))   AS before_account_name,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_after,  '$.account_name'))   AS after_account_name,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_before, '$.account_number')) AS before_account_number,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_after,  '$.account_number')) AS after_account_number,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_before, '$.category'))       AS before_category,
  JSON_UNQUOTE(JSON_EXTRACT(e.record_after,  '$.category'))       AS after_category
FROM eventlog e
WHERE e.table_name = 'Accounts';

--
-- Dumping data for table `eventlog`
--

LOCK TABLES `eventlog` WRITE;
/*!40000 ALTER TABLE `eventlog` DISABLE KEYS */;

-- Keep your existing seed row; account_id is NULL for backward compatibility
INSERT INTO `eventlog`
  (`user_id`,`table_name`,`action_type`,`record_before`,`record_after`,`event_time`)
VALUES
  (1,'Accounts','INSERT',NULL,'{\"balance\": 500.00, \"account_name\": \"Cash\", \"account_number\": \"1001\"}','2025-10-09 18:00:50');

/*!40000 ALTER TABLE `eventlog` ENABLE KEYS */;
UNLOCK TABLES;

 /*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
 /*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
 /*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
 /*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
 /*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET */;
 /*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
 /*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
 /*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
