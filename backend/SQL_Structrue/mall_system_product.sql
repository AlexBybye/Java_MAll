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
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `stock_quantity` int NOT NULL DEFAULT '0' COMMENT '库存量',
  `image_url` varchar(255) DEFAULT NULL COMMENT '商品图片URL',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否已删除 (软删除)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,26,'http://example.com/imageA.jpg',1,'2025-12-11 09:19:15','2025-12-12 22:20:30'),(2,'测试产品 - 手机 (更新)','最新款智能手机，高性能，长续航，支持5G',4299.00,150,'http://example.com/phone_updated.jpg',1,'2025-12-11 09:20:16','2025-12-12 21:23:03'),(3,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 09:20:26','2025-12-12 20:37:41'),(4,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 09:24:56','2025-12-12 22:20:30'),(5,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 09:35:36','2025-12-12 22:20:30'),(6,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 09:53:59','2025-12-12 22:20:30'),(7,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 09:55:55','2025-12-12 22:20:30'),(8,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 10:30:39','2025-12-12 22:20:30'),(9,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 10:39:36','2025-12-12 22:20:30'),(10,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 10:42:18','2025-12-12 22:20:30'),(11,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-11 10:44:16','2025-12-12 22:20:30'),(12,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 08:50:02','2025-12-12 22:20:30'),(13,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 09:13:18','2025-12-12 22:20:30'),(14,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 09:17:26','2025-12-12 22:20:30'),(15,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 09:30:33','2025-12-12 22:20:30'),(16,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 09:49:49','2025-12-12 22:20:30'),(17,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 11:27:31','2025-12-12 22:20:30'),(18,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 12:18:21','2025-12-12 22:20:30'),(19,'测试商品A - 订单核心测试','用于验证库存和订单事务',50.00,50,'http://example.com/imageA.jpg',1,'2025-12-12 18:00:51','2025-12-12 22:20:30'),(20,'测试产品 - 手机','最新款智能手机，高性能，长续航',3999.00,137,'http://example.com/phone.jpg',0,'2025-12-12 19:14:12','2025-12-13 17:33:31'),(21,'谢bro','独一无二，值得拥有',200.00,0,NULL,1,'2025-12-13 14:00:16','2025-12-13 14:12:54'),(22,'毕盛','贤德之，慎行之，独一无二，你值得拥有',1000000.00,0,'https://shorturl.asia/zh/TWU1v',1,'2025-12-13 14:14:49','2025-12-13 17:53:36'),(23,'手表','时间流转，你我不变',900.00,8,'https://shorturl.asia/h38Hn',0,'2025-12-13 15:02:36','2025-12-13 17:33:31'),(24,'华工计算机天王的三角洲账号','喜欢么？拍了我也不发货',4000.00,2,'https://shorturl.asia/6F3mO',0,'2025-12-13 15:04:58','2025-12-13 16:41:58');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-14  2:43:24
