-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: zinex
-- ------------------------------------------------------
-- Server version	8.0.45

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
-- Table structure for table `accounts`
--

CREATE DATABASE IF NOT EXISTS zinex;
USE zinex;

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
    `deposit_krw` bigint NOT NULL DEFAULT '0',
    `version` bigint NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT NULL,
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint unsigned NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_user_id` (`user_id`),
    CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `chk_accounts_non_negative` CHECK ((`deposit_krw` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `accounts_hold`
--

DROP TABLE IF EXISTS `accounts_hold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts_hold` (
     `id` bigint unsigned NOT NULL AUTO_INCREMENT,
     `user_id` bigint unsigned NOT NULL,
     `hold_krw` bigint NOT NULL,
     `status` enum('ACTIVE','RELEASED') NOT NULL DEFAULT 'ACTIVE',
     `reserved_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `released_at` timestamp NULL DEFAULT NULL,
     `release_reason` enum('CANCELLED','FILLED','FAILED','EXPIRED','ADJUSTED') DEFAULT NULL,
     `order_id` bigint unsigned NOT NULL,
     PRIMARY KEY (`id`),
     KEY `fk_accounts_hold_user` (`user_id`),
     KEY `fk_order_id` (`order_id`),
     CONSTRAINT `fk_accounts_hold_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
     CONSTRAINT `fk_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
     CONSTRAINT `chk_accounts_hold_amount` CHECK ((`hold_krw` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
      `type` enum('MARKET','SECTOR','INDUSTRY','THEME','TAG') NOT NULL,
      `name` varchar(255) NOT NULL,
      `parent_id` bigint unsigned DEFAULT NULL,
      `sort_order` int NOT NULL DEFAULT '0',
      `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `updated_at` timestamp NULL DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uq_categories_type_name` (`type`,`name`),
      KEY `fk_categories_parent` (`parent_id`),
      CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fills`
--

DROP TABLE IF EXISTS `fills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fills` (
     `id` bigint unsigned NOT NULL AUTO_INCREMENT,
     `order_id` bigint unsigned NOT NULL,
     `user_id` bigint unsigned NOT NULL,
     `stock_id` bigint unsigned NOT NULL,
     `side` enum('BUY','SELL') NOT NULL,
     `quantity` bigint NOT NULL,
     `price_krw` bigint NOT NULL,
     `fee_krw` bigint NOT NULL DEFAULT '0',
     `executed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     KEY `fk_fills_order` (`order_id`),
     KEY `fk_fills_stock` (`stock_id`),
     KEY `fk_fills_user` (`user_id`),
     CONSTRAINT `fk_fills_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
     CONSTRAINT `fk_fills_stock` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
     CONSTRAINT `fk_fills_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
     CONSTRAINT `chk_fills_money` CHECK (((`price_krw` > 0) and (`fee_krw` >= 0))),
     CONSTRAINT `chk_fills_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `holdings`
--

DROP TABLE IF EXISTS `holdings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holdings` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `user_id` bigint unsigned NOT NULL,
    `stock_id` bigint unsigned NOT NULL,
    `quantity` bigint NOT NULL DEFAULT '0',
    `avg_price_krw` bigint NOT NULL DEFAULT '0',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_holdings_stock` (`stock_id`),
    KEY `fk_holdings_user` (`user_id`),
    CONSTRAINT `fk_holdings_stock` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_holdings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `chk_holdings_avg` CHECK ((`avg_price_krw` >= 0)),
    CONSTRAINT `chk_holdings_qty` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
      `user_id` bigint unsigned NOT NULL,
      `stock_id` bigint unsigned NOT NULL,
      `side` enum('BUY','SELL') NOT NULL,
      `order_type` enum('LIMIT','MARKET') NOT NULL DEFAULT 'LIMIT',
      `status` enum('NEW','OPEN','CANCELLED','FILLED','FAILED','EXPIRED') NOT NULL DEFAULT 'NEW',
      `quantity` bigint NOT NULL,
      `limit_price_krw` bigint DEFAULT NULL,
      `filled_quantity` bigint NOT NULL DEFAULT '0',
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `updated_at` timestamp NULL DEFAULT NULL,
      `cancelled_at` timestamp NULL DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `fk_orders_stock` (`stock_id`),
      KEY `fk_orders_user` (`user_id`),
      CONSTRAINT `fk_orders_stock` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
      CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
      CONSTRAINT `chk_orders_qty` CHECK (((`quantity` > 0) and (`filled_quantity` >= 0) and (`filled_quantity` <= `quantity`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_category`
--

DROP TABLE IF EXISTS `stock_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_category` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
      `stock_id` bigint unsigned NOT NULL,
      `category_id` bigint unsigned NOT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uq_stock_category_pair` (`stock_id`,`category_id`),
      KEY `fk_stock_category_category` (`category_id`),
      CONSTRAINT `fk_stock_category_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
      CONSTRAINT `fk_stock_category_stock` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stocks`
--

DROP TABLE IF EXISTS `stocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stocks` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
      `symbol` varchar(255) NOT NULL,
      `name` varchar(255) NOT NULL,
      `market` varchar(255) NOT NULL,
      `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `updated_at` timestamp NULL DEFAULT NULL,
      `isin` varchar(12) NOT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `isin_UNIQUE` (`isin`),
      UNIQUE KEY `symbol_market_UNIQUE` (`symbol`,`market`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trade_logs`
--

DROP TABLE IF EXISTS `trade_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trade_logs` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
      `user_id` bigint unsigned DEFAULT NULL,
      `order_id` bigint unsigned DEFAULT NULL,
      `fill_id` bigint unsigned DEFAULT NULL,
      `event_type` varchar(255) NOT NULL,
      `payload_json` json DEFAULT NULL,
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `updated_at` timestamp NULL DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `fk_trade_logs_fill` (`fill_id`),
      KEY `fk_trade_logs_order` (`order_id`),
      KEY `fk_trade_logs_user` (`user_id`),
      CONSTRAINT `fk_trade_logs_fill` FOREIGN KEY (`fill_id`) REFERENCES `fills` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
      CONSTRAINT `fk_trade_logs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
      CONSTRAINT `fk_trade_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
     `id` bigint unsigned NOT NULL AUTO_INCREMENT,
     `email` varchar(255) NOT NULL,
     `password` varchar(255) NOT NULL,
     `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
     `status` enum('ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'ACTIVE',
     `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `updated_at` timestamp NULL DEFAULT NULL,
     `name` varchar(50) NOT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-10 20:27:00
