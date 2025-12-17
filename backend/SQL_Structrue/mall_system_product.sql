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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'华工计算机天王的三角洲账号','心动了？你拍了我也不卖给你hh',2000.00,1,'https://tse2-mm.cn.bing.net/th/id/OIP-C.0LSpI310mdmvmQWYgINKXAHaEK?w=243&h=180&c=7&r=0&o=7&cb=ucfimg2&dpr=1.8&pid=1.7&rm=3&ucfimg=1',0,'2025-12-13 20:31:25','2025-12-13 23:40:37'),(2,'浮力设备','潮汐监狱特产，食之无味，弃之可惜，遗之仓库，留之不知所时',1900.00,17,'https://playerhub.df.qq.com/playerhub/60004/object/15080050144.png',0,'2025-12-13 20:32:42','2025-12-13 23:40:37'),(3,'印象派名画','什么是印象派？你说毕加索？那为什么一模一样50幅？故宫一件我一件',3900.00,41,'https://ts1.tc.mm.bing.net/th/id/OIP-C.T_NTdGdvvfMWDSdMODAWWgAAAA?w=172&h=211&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.8&pid=3.1&rm=2&ucfimg=1',0,'2025-12-13 20:35:05','2025-12-13 23:48:52'),(4,'卫星锅','相传在有电视的时候，在机顶盒时代曾经涌现大街小巷',200.00,195,'https://ts1.tc.mm.bing.net/th/id/OIP-C.paEsbb6d843LLvtNLVIl_QAAAA?w=160&h=211&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.8&pid=3.1&rm=2&ucfimg=1',0,'2025-12-13 20:36:03','2025-12-13 23:46:42'),(5,'man德尔超算单元','还在担心华工的老旧计算卡无法满足科研需求？只需一块，造就未来--HAAVK',6500.00,90,'https://tse2-mm.cn.bing.net/th/id/OIP-C.TmQexu_iEsiqaWJr3RL9TAAAAA?w=303&h=177&c=7&r=0&o=7&cb=ucfimg2&dpr=1.8&pid=1.7&rm=3&ucfimg=1',0,'2025-12-13 20:37:01','2025-12-13 23:46:42'),(6,'刀片服务器','妈妈再也不用担心凌晨断网后我的网络和我一样总丢包了',12500.00,25,'https://ts1.tc.mm.bing.net/th/id/OIP-C.oT2TLcWkYrtlYbnRwbGTSQAAAA?w=160&h=211&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.8&pid=3.1&rm=2&ucfimg=1',0,'2025-12-13 20:37:52','2025-12-14 00:33:10'),(7,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,999,'http://example.com/testA_202412.jpg',0,'2025-12-14 00:01:47','2025-12-14 00:33:10'),(8,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,998,'http://example.com/testB_202412.jpg',0,'2025-12-14 00:01:47','2025-12-14 00:33:10'),(9,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,1000,'http://example.com/testA_202412.jpg',1,'2025-12-14 00:06:20','2025-12-14 00:15:32'),(10,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,1000,'http://example.com/testB_202412.jpg',1,'2025-12-14 00:06:20','2025-12-14 00:15:34'),(11,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,1000,'http://example.com/testA_202412.jpg',1,'2025-12-14 00:07:37','2025-12-14 00:15:35'),(12,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,1000,'http://example.com/testB_202412.jpg',1,'2025-12-14 00:07:37','2025-12-14 00:15:28'),(13,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,1000,'http://example.com/testA_202412.jpg',1,'2025-12-14 00:09:07','2025-12-14 00:15:27'),(14,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,1000,'http://example.com/testB_202412.jpg',1,'2025-12-14 00:09:07','2025-12-14 00:15:25'),(15,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,1000,'http://example.com/testA_202412.jpg',1,'2025-12-14 00:09:22','2025-12-14 00:15:23'),(16,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,1000,'http://example.com/testB_202412.jpg',1,'2025-12-14 00:09:22','2025-12-14 00:15:21'),(17,'测试商品A - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',50.00,1000,'http://example.com/testA_202412.jpg',1,'2025-12-14 00:12:26','2025-12-14 00:15:11'),(18,'测试商品B - 202412订单测试','用于2024年12月开始的订单测试，避免干扰现有商品',80.00,1000,'http://example.com/testB_202412.jpg',1,'2025-12-14 00:12:26','2025-12-14 00:15:09');
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

-- Dump completed on 2025-12-15 21:06:17
