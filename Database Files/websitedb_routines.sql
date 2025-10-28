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

-- ============================================================
-- View: accounts_admin_view   (kept from your current routines)
-- ============================================================

-- Temporary view structure
DROP TABLE IF EXISTS `accounts_admin_view`;
/*!50001 DROP VIEW IF EXISTS `accounts_admin_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `accounts_admin_view` AS SELECT
 1 AS `account_id`,
 1 AS `account_name`,
 1 AS `account_number`,
 1 AS `description`,
 1 AS `normal_side`,
 1 AS `category`,
 1 AS `subcategory`,
 1 AS `initial_balance`,
 1 AS `debit`,
 1 AS `credit`,
 1 AS `balance`,
 1 AS `date_added`,
 1 AS `user_id`,
 1 AS `display_order`,
 1 AS `statement_type`,
 1 AS `comment`*/;
SET character_set_client = @saved_cs_client;

-- Final view structure
/*!50001 DROP VIEW IF EXISTS `accounts_admin_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
-- (omit DEFINER to avoid env-specific errors)
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50001 SQL SECURITY DEFINER */
/*!50001 VIEW `accounts_admin_view` AS
  SELECT `accounts`.`account_id`   AS `account_id`,
         `accounts`.`account_name` AS `account_name`,
         `accounts`.`account_number` AS `account_number`,
         `accounts`.`description`  AS `description`,
         `accounts`.`normal_side`  AS `normal_side`,
         `accounts`.`category`     AS `category`,
         `accounts`.`subcategory`  AS `subcategory`,
         `accounts`.`initial_balance` AS `initial_balance`,
         `accounts`.`debit`        AS `debit`,
         `accounts`.`credit`       AS `credit`,
         `accounts`.`balance`      AS `balance`,
         `accounts`.`date_added`   AS `date_added`,
         `accounts`.`user_id`      AS `user_id`,
         `accounts`.`display_order` AS `display_order`,
         `accounts`.`statement_type` AS `statement_type`,
         `accounts`.`comment`      AS `comment`
  FROM `accounts`
  WHERE `accounts`.`user_id` IN (
    SELECT `users`.`user_id` FROM `users` WHERE (`users`.`role` = 'Admin')
  )
*/;
/*!50001 SET character_set_client      = @saved_cs_client */
/*!50001 SET character_set_results     = @saved_cs_results */
/*!50001 SET collation_connection      = @saved_col_connection */;

-- ============================================================
-- Triggers: audit Accounts into eventlog (before/after images)
-- ============================================================

DELIMITER $$

-- INSERT -> log CREATE (no before image)
DROP TRIGGER IF EXISTS trg_accounts_ai_create $$
CREATE TRIGGER trg_accounts_ai_create
AFTER INSERT ON accounts
FOR EACH ROW
BEGIN
  INSERT INTO eventlog (
    user_id, table_name, action_type, account_id, record_before, record_after
  ) VALUES (
    COALESCE(@app_user_id, NEW.user_id, 0),
    'Accounts',
    'INSERT',
    NEW.account_id,
    NULL,
    JSON_OBJECT(
      'account_id',      NEW.account_id,
      'account_name',    NEW.account_name,
      'account_number',  NEW.account_number,
      'description',     NEW.description,
      'normal_side',     NEW.normal_side,
      'category',        NEW.category,
      'subcategory',     NEW.subcategory,
      'initial_balance', CAST(NEW.initial_balance AS CHAR),
      'debit',           CAST(NEW.debit AS CHAR),
      'credit',          CAST(NEW.credit AS CHAR),
      'statement_type',  NEW.statement_type,
      'user_id',         NEW.user_id,
      'date_added',      DATE_FORMAT(NEW.date_added, '%Y-%m-%d %H:%i:%s')
    )
  );
END $$

-- UPDATE (name changed) -> log before & after + who + when
DROP TRIGGER IF EXISTS trg_accounts_bu_update_name $$
CREATE TRIGGER trg_accounts_bu_update_name
BEFORE UPDATE ON accounts
FOR EACH ROW
BEGIN
  IF NEW.account_name <> OLD.account_name THEN
    INSERT INTO eventlog (
      user_id, table_name, action_type, account_id, record_before, record_after
    ) VALUES (
      COALESCE(@app_user_id, NEW.user_id, 0),
      'Accounts',
      'UPDATE',
      OLD.account_id,
      JSON_OBJECT(
        'account_id',     OLD.account_id,
        'account_name',   OLD.account_name,
        'account_number', OLD.account_number,
        'description',    OLD.description,
        'normal_side',    OLD.normal_side,
        'category',       OLD.category,
        'subcategory',    OLD.subcategory
      ),
      JSON_OBJECT(
        'account_id',     NEW.account_id,
        'account_name',   NEW.account_name,
        'account_number', NEW.account_number,
        'description',    NEW.description,
        'normal_side',    NEW.normal_side,
        'category',       NEW.category,
        'subcategory',    NEW.subcategory
      )
    );
  END IF;
END $$

-- (Optional) DELETE -> keep a final before image
DROP TRIGGER IF EXISTS trg_accounts_bd_delete $$
CREATE TRIGGER trg_accounts_bd_delete
BEFORE DELETE ON accounts
FOR EACH ROW
BEGIN
  INSERT INTO eventlog (
    user_id, table_name, action_type, account_id, record_before, record_after
  ) VALUES (
    COALESCE(@app_user_id, OLD.user_id, 0),
    'Accounts',
    'DELETE',
    OLD.account_id,
    JSON_OBJECT(
      'account_id',     OLD.account_id,
      'account_name',   OLD.account_name,
      'account_number', OLD.account_number,
      'description',    OLD.description,
      'normal_side',    OLD.normal_side,
      'category',       OLD.category,
      'subcategory',    OLD.subcategory
    ),
    NULL
  );
END $$

DELIMITER ;

-- Restore session settings
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
 /*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
 /*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
 /*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
 /*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
 /*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESUL_*
