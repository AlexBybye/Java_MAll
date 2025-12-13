-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: mall_system
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `order_master`
--

DROP TABLE IF EXISTS `order_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_master` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID (主键)',
  `customer_id` int NOT NULL COMMENT '客户ID (外键)',
  `order_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `shipping_address` varchar(255) NOT NULL COMMENT '收货地址',
  `order_status` varchar(50) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态 (PENDING, PAID, SHIPPED, COMPLETED, CANCELLED)',
  PRIMARY KEY (`order_id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `order_master_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_master`
--

LOCK TABLES `order_master` WRITE;
/*!40000 ALTER TABLE `order_master` DISABLE KEYS */;
INSERT INTO `order_master` VALUES (3,1,'2025-12-12 20:14:20',400.00,'北京市海淀区中关村测试点','shipped'),(4,1,'2025-12-12 20:18:36',100.00,'北京市海淀区中关村测试点','confirmed'),(5,5,'2025-12-13 04:02:36',200.00,'北京市海淀区中关村大街1号','cancelled'),(6,5,'2025-12-13 04:31:31',100.00,'北京市海淀区中关村大街1号','confirmed'),(7,4,'2025-12-14 00:29:12',1800.00,'广东省广州市番禺区小谷围街道外环东路382号','delivered'),(8,4,'2025-12-14 00:41:58',5800.00,'辽宁省盘锦市兴隆台区振兴街道香稻路？号','pending'),(9,4,'2025-12-14 01:20:51',900.00,'河北省秦皇岛市东北大学秦皇岛分校','delivered'),(10,4,'2025-12-14 01:22:19',900.00,'广东省广州市番禺区小谷围街道外环东路382号','shipped'),(11,4,'2025-12-14 01:33:31',12897.00,'广东省广州市番禺区小谷围街道外环东路382号','confirmed'),(12,4,'2025-12-14 01:53:00',1000000.00,'1','delivered');
/*!40000 ALTER TABLE `order_master` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-14  2:43:23
