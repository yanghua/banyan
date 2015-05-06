CREATE DATABASE  IF NOT EXISTS `banyan_DB` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;
USE `banyan_DB`;
-- MySQL dump 10.13  Distrib 5.6.17, for osx10.6 (i386)
--
-- Host: 172.16.206.17    Database: banyan_DB
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `APP`
--

DROP TABLE IF EXISTS `APP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `APP` (
  `APP_ID` varchar(20) NOT NULL,
  `NAME` varchar(100) DEFAULT NULL,
  `CREATOR` varchar(20) DEFAULT NULL,
  `FROM_DATE` datetime DEFAULT NULL,
  `THRU_DATE` datetime DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`APP_ID`),
  KEY `fk_App_UserLogin` (`CREATOR`),
  KEY `APP_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `APP_TXCRTS` (`CREATED_TX_STAMP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `APP`
--

LOCK TABLES `APP` WRITE;
/*!40000 ALTER TABLE `APP` DISABLE KEYS */;
INSERT INTO `APP` VALUES ('1','server','admin','2015-03-22 09:15:45',NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('100','cms','banyanAdmin','2015-03-28 10:15:45',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('101','oa','banyanAdmin','2015-03-28 10:15:45',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('2','console','admin','2015-03-22 09:15:45',NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('3','emap','banyanAdmin','2015-03-16 10:15:45',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('4','erp','banyanAdmin','2015-03-16 10:15:45',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('5','amp','banyanAdmin','2015-03-31 03:28:30',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('6','logSys','banyanAdmin','2015-04-27 13:28:30',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('7','remoteAmp','banyanAdmin','2015-04-28 20:10:30',NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40');
/*!40000 ALTER TABLE `APP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUDIT_TYPE`
--

DROP TABLE IF EXISTS `AUDIT_TYPE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUDIT_TYPE` (
  `AUDIT_TYPE_CODE` varchar(100) NOT NULL,
  `AUDIT_TYPE_NAME` varchar(100) DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`AUDIT_TYPE_CODE`),
  KEY `AUDIT_TYPE_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `AUDIT_TYPE_TXCRTS` (`CREATED_TX_STAMP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUDIT_TYPE`
--

LOCK TABLES `AUDIT_TYPE` WRITE;
/*!40000 ALTER TABLE `AUDIT_TYPE` DISABLE KEYS */;
INSERT INTO `AUDIT_TYPE` VALUES ('AUDIT_BEFORE','???','2015-04-28 19:48:16','2015-04-28 19:48:16','2015-04-28 19:47:17','2015-04-28 19:47:17'),('AUDIT_FAILED','?????','2015-04-28 19:48:16','2015-04-28 19:48:16','2015-04-28 19:47:17','2015-04-28 19:47:17'),('AUDIT_SUCCESS','????','2015-04-28 19:48:16','2015-04-28 19:48:16','2015-04-28 19:47:17','2015-04-28 19:47:17');
/*!40000 ALTER TABLE `AUDIT_TYPE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CONFIG`
--

DROP TABLE IF EXISTS `CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONFIG` (
  `CONFIG_ID` varchar(20) NOT NULL,
  `ITEM_KEY` varchar(100) DEFAULT NULL,
  `ITEM_VALUE` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`CONFIG_ID`),
  KEY `CONFIG_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `CONFIG_TXCRTS` (`CREATED_TX_STAMP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CONFIG`
--

LOCK TABLES `CONFIG` WRITE;
/*!40000 ALTER TABLE `CONFIG` DISABLE KEYS */;
INSERT INTO `CONFIG` VALUES ('1','messagebus.client.host','172.16.206.29','client','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('2','messagebus.client.port','5672','client','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17');
/*!40000 ALTER TABLE `CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NODE`
--

DROP TABLE IF EXISTS `NODE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NODE` (
  `NODE_ID` varchar(20) NOT NULL,
  `SECRET` varchar(255) DEFAULT NULL,
  `NAME` varchar(100) DEFAULT NULL,
  `VALUE` varchar(255) DEFAULT NULL,
  `PARENT_ID` varchar(20) DEFAULT NULL,
  `TYPE` varchar(20) DEFAULT NULL,
  `ROUTER_TYPE` varchar(255) DEFAULT NULL,
  `ROUTING_KEY` varchar(255) DEFAULT NULL,
  `AVAILABLE` char(1) DEFAULT NULL,
  `IS_INNER` char(1) DEFAULT NULL,
  `IS_VIRTUAL` char(1) DEFAULT NULL,
  `COMMUNICATE_TYPE` varchar(100) DEFAULT NULL,
  `CREATOR` varchar(20) DEFAULT NULL,
  `APP_ID` varchar(20) DEFAULT NULL,
  `RATE_LIMIT` varchar(60) DEFAULT NULL,
  `THRESHOLD` varchar(60) DEFAULT NULL,
  `MSG_BODY_SIZE` varchar(60) DEFAULT NULL,
  `TTL` varchar(60) DEFAULT NULL,
  `TTL_PER_MSG` varchar(60) DEFAULT NULL,
  `AUDIT_TYPE_CODE` varchar(100) DEFAULT NULL,
  `CAN_BROADCAST` char(1) DEFAULT NULL,
  `FROM_DATE` datetime DEFAULT NULL,
  `THRU_DATE` datetime DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`NODE_ID`),
  KEY `fk_Node_UserLogin` (`CREATOR`),
  KEY `fk_Node_AuditType` (`AUDIT_TYPE_CODE`),
  KEY `fk_Node_App` (`APP_ID`),
  KEY `NODE_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `NODE_TXCRTS` (`CREATED_TX_STAMP`),
  CONSTRAINT `fk_Node_App` FOREIGN KEY (`APP_ID`) REFERENCES `APP` (`APP_ID`),
  CONSTRAINT `fk_Node_AuditType` FOREIGN KEY (`AUDIT_TYPE_CODE`) REFERENCES `AUDIT_TYPE` (`AUDIT_TYPE_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NODE`
--

LOCK TABLES `NODE` WRITE;
/*!40000 ALTER TABLE `NODE` DISABLE KEYS */;
INSERT INTO `NODE` VALUES ('1',NULL,'proxy','exchange.proxy','-1','0','fanout',NULL,'1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('10','iqwjasdfklakqoiajsidfoasidjoqw','serverInfoResponse','queue.proxy.message.reqresp.serverInfoResponse','6','1',NULL,'routingkey.proxy.message.reqresp.serverInfoResponse','1','0','0','response','banyanAdmin','1',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('100','kjhqwehuksadhjkhkqfkjshduwuhas','cmsDemoProduce','queue.proxy.message.procon.cmsDemoProduce','4','1',NULL,'routingkey.proxy.message.procon.cmsDemoProduce','1','0','1','produce','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('101','qiuiuchvqihuchvqnuicoaudhfieqi','cmsDemoConsume','queue.proxy.message.procon.cmsDemoConsume','4','1',NULL,'routingkey.proxy.message.procon.cmsDemoConsume','1','0','0','consume','banyanAdmin','4','1000','20000','2000',NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('102','ijwlieulcaisduhfilhliuhdfbsjhx','cmsDemoPublish','queue.proxy.message.pubsub.cmsDemoPublish','5','1',NULL,'routingkey.proxy.message.pubsub.cmsDemoPublish','1','0','1','publish','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('103','jnsdlkblkAWEHLIDVBAJSDlohkjasn','cmsDemoSubscribe','queue.proxy.message.pubsub.cmsDemoSubscribe','5','1',NULL,'routingkey.proxy.message.pubsub.cmsDemoSubscribe','1','0','0','subscribe','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('104','mfqemwqkjdkuhxqnklhawjebuwyiud','oaDemoProduce','queue.proxy.message.procon.oaDemoProduce','4','1',NULL,'routingkey.proxy.message.procon.oaDemoProduce','1','0','1','produce','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('105','miwenasuhqiboudfqiuhfiuhdfquha','oaDemoConsume','queue.proxy.message.procon.oaDemoConsume','4','1',NULL,'routingkey.proxy.message.procon.oaDemoConsume','1','0','0','consume','banyanAdmin','4','1000','20000','2000',NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('106','ewoiasnflieunjkdflqienflsdifln','oaDemoPublish','queue.proxy.message.pubsub.oaDemoPublish','5','1',NULL,'routingkey.proxy.message.pubsub.oaDemoPublish','1','0','1','publish','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('107','mnxcblawkebflifiauhfqliwuhflan','oaDemoSubscribe','queue.proxy.message.pubsub.oaDemoSubscribe','5','1',NULL,'routingkey.proxy.message.pubsub.oaDemoSubscribe','1','0','0','subscribe','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_BEFORE',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('11','nadjfqulaudhfkauwaudhfakqajd','serverCmdResponse','queue.proxy.message.reqresp.serverCmdResponse','6','1',NULL,'routingkey.proxy.message.reqresp.serverCmdResponse','1','0','0','response','banyanAdmin','1',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('12','miuhqihusahdfuhaksjhfuiqweka','serverInfoRequest','queue.proxy.message.reqresp.serverInfoRequest','6','1',NULL,'routingkey.proxy.message.reqresp.serverInfoRequest','1','0','0','request','banyanAdmin','2',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('13','lauhsdjkfhqiuwequhiausdfhuah','serverCmdRequest','queue.proxy.message.reqresp.serverCmdRequest','6','1',NULL,'routingkey.proxy.message.reqresp.serverCmdRequest','1','0','0','request','banyanAdmin','2',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('2',NULL,'log','exchange.proxy.log','1','0','fanout',NULL,'1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('200','iojawdnaisdflknoiankjfdblaidcas','appDataQueue','queue.proxy.message.procon.appDataQueue','4','1',NULL,'routingkey.proxy.message.procon.appDataQueue','1','0','0','produce-consume','banyanAdmin','5',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('201','qliudwnfljainfqlhalsdbfqlwehfbl','appErrorEventQueue','queue.proxy.message.procon.appErrorEventQueue','4','1',NULL,'routingkey.proxy.message.procon.appErrorEventQueue','1','0','0','consume','banyanAdmin','5',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('3',NULL,'message','exchange.proxy.message','1','0','topic',NULL,'1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('300','liredkifnhaqweuhfqbablwddflawea','appErrorEventProducer','queue.proxy.message.procon.appErrorEventProducer','4','1',NULL,'routingkey.proxy.message.procon.appErrorEventProducer','1','0','1','produce','banyanAdmin','6',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('4',NULL,'procon','exchange.proxy.message.procon','3','0','topic','routingkey.proxy.message.procon.#','1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('400','jbdlhbcLQHDBLASBDALsdblASHBDjhn','appErrorRemoteQProducer','queue.proxy.message.procon.appErrorRemoteQProducer','4','1',NULL,'routingkey.proxy.message.procon.appErrorRemoteQProducer','1','0','1','produce','banyanAdmin','7',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('401','lkaqnwejnfalwefjbsdfkjasnfjqlas','appErrorRemoteQ','queue.proxy.message.procon.appErrorRemoteQ','4','1',NULL,'routingkey.proxy.message.procon.appErrorRemoteQ','1','0','0','consume','banyanAdmin','7',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('43','kljasdoifqoikjhhhqwhebasdfasdf','erpDemoProduce','queue.proxy.message.procon.erpDemoProduce','4','1',NULL,'routingkey.proxy.message.procon.erpDemoProduce','1','0','1','produce','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS','1',NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('44','kjhasdfhlkuqjhgaebjhasgdfabfak','erpDemoConsume','queue.proxy.message.procon.erpDemoConsume','4','1',NULL,'routingkey.proxy.message.procon.erpDemoConsume','1','0','0','consume','banyanAdmin','4','1000','20000','2000',NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('45','jnmzqwemnjaksdfqjnkajfjasndfnw','erpDemoProduce-Consume','queue.proxy.message.procon.erpDemoProduce-Consume','4','1',NULL,'routingkey.proxy.message.procon.erpDemoProduce-Consume','1','0','0','produce-consume','banyanAdmin','4',NULL,'100','500',NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('46','iuoqiwejicaoisfaisfbsqewnfjnfa','erpDemoRequest','queue.proxy.message.reqresp.erpDemoRequest','6','1',NULL,'routingkey.proxy.message.reqresp.erpDemoRequest','1','0','1','request','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('47','muciasnajjkasbdfbaskjdfkjkasja','erpDemoResponse','queue.proxy.message.reqresp.erpDemoResponse','6','1',NULL,'routingkey.proxy.message.reqresp.erpDemoResponse','1','0','0','response','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('48','qiwernjknfaawenkjasdfkjnakllas','erpDemoRequest-Response','queue.proxy.message.reqresp.erpDemoRequest-Response','6','1',NULL,'routingkey.proxy.message.reqresp.erpDemoRequest-Response','1','0','0','request-response','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('49','oiqwenncuicnsdfuasdfnkajkwqowe','erpDemoPublish','queue.proxy.message.pubsub.erpDemoPublish','5','1',NULL,'routingkey.proxy.message.pubsub.erpDemoPublish','1','0','1','publish','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('5',NULL,'pubsub','exchange.proxy.message.pubsub','3','0','topic','routingkey.proxy.message.pubsub.#','1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('50','nckljsenlkjanefluiwnlanfmsdfas','erpDemoSubscribe','queue.proxy.message.pubsub.erpDemoSubscribe','5','1',NULL,'routingkey.proxy.message.pubsub.erpDemoSubscribe','1','0','0','subscribe','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('51','xzmjsdafnkjanuwqijpasdfnanlkna','erpDemoPublish-Subscribe','queue.proxy.message.pubsub.erpDemoPublish-Subscribe','5','1',NULL,'routingkey.proxy.message.pubsub.erpDemoPublish-Subscribe','1','0','0','publish-subscribe','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('52','kliwhiduhaiucvarkjajksdbfkjabw','erpDemoRpcRequest','queue.proxy.message.rpc.erpDemoRpcRequest','7','1',NULL,'routingkey.proxy.message.rpc.erpDemoRpcRequest','1','0','1','rpcrequest','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('53','jhliquwdlaisudfqbjhasdfulquias','erpDemoRpcResponse','queue.proxy.message.rpc.erpDemoRpcResponse','7','1',NULL,'routingkey.proxy.message.rpc.erpDemoRpcResponse','1','0','0','rpcresponse','banyanAdmin','4',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('6',NULL,'reqresp','exchange.proxy.message.reqresp','3','0','topic','routingkey.proxy.message.reqresp.#','1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('7',NULL,'rpc','exchange.proxy.message.rpc','3','0','topic','routingkey.proxy.message.rpc.#','1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('73','muqwejlaksdfkljaliqwejflkasdfs','emapDemoProduce','queue.proxy.message.procon.emapDemoProduce','4','1',NULL,'routingkey.proxy.message.procon.emapDemoProduce','1','0','1','produce','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('74','zxdjnflakwenklasjdflkqpiasdfnj','emapDemoConsume','queue.proxy.message.procon.emapDemoConsume','4','1',NULL,'routingkey.proxy.message.procon.emapDemoConsume','1','0','0','consume','banyanAdmin','3','2000',NULL,'1000',NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('75','zxjnlasndflkjasdlflqwekjnflaks','emapDemoProduce-Consume','queue.proxy.message.procon.emapDemoProduce-Consume','4','1',NULL,'routingkey.proxy.message.procon.emapDemoProduce-Consume','1','0','0','produce-consume','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('76','mzxhblawdblulawkwkkwksifhlaasd','emapDemoRequest','queue.proxy.message.reqresp.emapDemoRequest','6','1',NULL,'routingkey.proxy.message.reqresp.emapDemoRequest','1','0','1','request','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('77','zxjhvclawenlkfhsladfnqpwenflak','emapDemoResponse','queue.proxy.message.reqresp.emapDemoResponse','6','1',NULL,'routingkey.proxy.message.reqresp.emapDemoResponse','1','0','0','response','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('78','xcvnbladflkasudflwepqowiejfoas','emapDemoRequest-Response','queue.proxy.message.reqresp.emapDemoRequest-Response','6','1',NULL,'routingkey.proxy.message.reqresp.emapDemoRequest-Response','1','0','0','request-response','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('79','zxcjvhlawenlkfhsalfqpiieiwqqsd','emapDemoPublish','queue.proxy.message.pubsub.emapDemoPublish','5','1',NULL,'routingkey.proxy.message.pubsub.emapDemoPublish','1','0','1','publish','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('8',NULL,'notification','exchange.proxy.message.notification','3','0','fanout','routingkey.proxy.message.notification.#','1','1',NULL,NULL,'banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('80','zxcnvblawelkusahdfqwiuhowefhnx','emapDemoSubscribe','queue.proxy.message.pubsub.emapDemoSubscribe','5','1',NULL,'routingkey.proxy.message.pubsub.emapDemoSubscribe','1','0','0','subscribe','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('81','zxcnvblaeflbwehiouhusduaflsuda','emapDemoPublish-Subscribe','queue.proxy.message.pubsub.emapDemoPublish-Subscribe','5','1',NULL,'routingkey.proxy.message.pubsub.emapDemoPublish-Subscribe','1','0','0','publish-subscribe','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('82','jnhiuerhskjdhfiasdkjhasdlkwerf','emapDemoRpcRequest','queue.proxy.message.rpc.emapDemoRpcRequest','7','1',NULL,'routingkey.proxy.message.rpc.emapDemoRpcRequest','1','0','0','rpcrequest','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('83','mshdfjbqwejhfgasdfbjqkygaksdfa','emapDemoRpcResponse','queue.proxy.message.rpc.emapDemoRpcResponse','7','1',NULL,'routingkey.proxy.message.rpc.emapDemoRpcResponse','1','0','0','rpcresponse','banyanAdmin','3',NULL,NULL,NULL,NULL,NULL,'AUDIT_SUCCESS',NULL,NULL,NULL,'2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('9','hkajhdfiuwxjdhakjdshuuuqoxdfasg','file','exchange.proxy.log.file','2','1',NULL,NULL,'1','1',NULL,'consume','banyanAdmin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17');
/*!40000 ALTER TABLE `NODE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NODE_AUDIT_HISTORY`
--

DROP TABLE IF EXISTS `NODE_AUDIT_HISTORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NODE_AUDIT_HISTORY` (
  `NODE_AUDIT_ID` varchar(20) NOT NULL,
  `NODE_ID` varchar(20) DEFAULT NULL,
  `AUDIT_TYPE_CODE` varchar(100) DEFAULT NULL,
  `AUDITOR` varchar(20) DEFAULT NULL,
  `FROM_DATE` datetime DEFAULT NULL,
  `THRU_DATE` datetime DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`NODE_AUDIT_ID`),
  KEY `fk_NodeAuditHistory_AuditType` (`AUDIT_TYPE_CODE`),
  KEY `fk_NodeAuditHistory_Node` (`NODE_ID`),
  KEY `fk_NodeAuditHistory_UserLogin` (`AUDITOR`),
  KEY `ND_ADT_HSTR_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `ND_ADT_HSTR_TXCRTS` (`CREATED_TX_STAMP`),
  CONSTRAINT `fk_NodeAuditHistory_Node` FOREIGN KEY (`NODE_ID`) REFERENCES `NODE` (`NODE_ID`),
  CONSTRAINT `fk_NodeAuditHistory_AuditType` FOREIGN KEY (`AUDIT_TYPE_CODE`) REFERENCES `AUDIT_TYPE` (`AUDIT_TYPE_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NODE_AUDIT_HISTORY`
--

LOCK TABLES `NODE_AUDIT_HISTORY` WRITE;
/*!40000 ALTER TABLE `NODE_AUDIT_HISTORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `NODE_AUDIT_HISTORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QUEUE_RATE_WARNING`
--

DROP TABLE IF EXISTS `QUEUE_RATE_WARNING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUEUE_RATE_WARNING` (
  `WARNING_ID` varchar(20) NOT NULL,
  `NODE_ID` varchar(20) DEFAULT NULL,
  `RATE_LIMIT` varchar(255) DEFAULT NULL,
  `REAL_RATE` varchar(255) DEFAULT NULL,
  `FROM_DATE` datetime DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`WARNING_ID`),
  KEY `fk_QueueRateWarning_Node` (`NODE_ID`),
  KEY `Q_RT_WRNNG_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `Q_RT_WRNNG_TXCRTS` (`CREATED_TX_STAMP`),
  CONSTRAINT `fk_QueueRateWarning_Node` FOREIGN KEY (`NODE_ID`) REFERENCES `NODE` (`NODE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QUEUE_RATE_WARNING`
--

LOCK TABLES `QUEUE_RATE_WARNING` WRITE;
/*!40000 ALTER TABLE `QUEUE_RATE_WARNING` DISABLE KEYS */;
INSERT INTO `QUEUE_RATE_WARNING` VALUES ('1','74','2000','4000','2015-03-28 10:12:11','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('2','74','2000','4321','2015-03-28 11:02:11','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('3','74','2000','5242','2015-03-28 12:43:11','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('4','74','2000','4324','2015-03-28 13:45:11','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('5','74','2000','7645','2015-03-28 14:23:11','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40');
/*!40000 ALTER TABLE `QUEUE_RATE_WARNING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SINK`
--

DROP TABLE IF EXISTS `SINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SINK` (
  `SINK_ID` varchar(20) NOT NULL,
  `TOKEN` varchar(255) DEFAULT NULL,
  `FLOW_FROM` varchar(20) DEFAULT NULL,
  `FROM_COMMUNICATE_TYPE` varchar(100) DEFAULT NULL,
  `FLOW_TO` varchar(20) DEFAULT NULL,
  `TO_COMMUNICATE_TYPE` varchar(100) DEFAULT NULL,
  `ENABLE` char(1) DEFAULT NULL,
  `CREATOR` varchar(20) DEFAULT NULL,
  `AUDIT_TYPE_CODE` varchar(100) DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`SINK_ID`),
  KEY `fk_Sink_UserLogin` (`CREATOR`),
  KEY `fk_Sink_AuditType` (`AUDIT_TYPE_CODE`),
  KEY `fk_Sink_Node_flowFrom` (`FLOW_FROM`),
  KEY `fk_Sink_Node_flowTo` (`FLOW_TO`),
  KEY `SINK_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `SINK_TXCRTS` (`CREATED_TX_STAMP`),
  CONSTRAINT `fk_Sink_Node_flowTo` FOREIGN KEY (`FLOW_TO`) REFERENCES `NODE` (`NODE_ID`),
  CONSTRAINT `fk_Sink_AuditType` FOREIGN KEY (`AUDIT_TYPE_CODE`) REFERENCES `AUDIT_TYPE` (`AUDIT_TYPE_CODE`),
  CONSTRAINT `fk_Sink_Node_flowFrom` FOREIGN KEY (`FLOW_FROM`) REFERENCES `NODE` (`NODE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SINK`
--

LOCK TABLES `SINK` WRITE;
/*!40000 ALTER TABLE `SINK` DISABLE KEYS */;
INSERT INTO `SINK` VALUES ('1','masdjfqiowieqooeirfajhfihfweld','12','request','10','response','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('10','nclajsdljhqiuwehfiusaiudfhiausd','49','publish','80','subscribe','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('100','iuwqijshdiuqjbuisdfiuewiuhiuiqw','100',NULL,'105',NULL,'1','banyanAdmin','AUDIT_BEFORE','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('101','akjfqkanlkjsdhfiqnlkajdhaskjasd','104',NULL,'101',NULL,'1','banyanAdmin','AUDIT_BEFORE','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('11','njafjqwhefuyasdafkbdgfkauyeqwku','79','publish','50','subscribe','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('12','migurhiquehoiusdhfouwiefhasboudy','79','publish','80','subscribe','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('13','qjenfiuqwefqiefoiusdfie2hfbwhfbd','300','produce','201','consume','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('14','kjandkjfnasdjkfasndfalkjbwejlaas','400','produce','401','consume','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('2','masuehiuiauhfiuqoquhaisudfhuqe','13','request','11','response','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17','2015-04-28 19:47:17'),('3','hlkasjdhfkqlwhlfalksjdhgssssas','43','produce','74','consume','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('4','jhlkasdfkjhasdfqwkasdfjqkwjhas','73','produce','45','produce-consume','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('5','cakjdhfjasdflqjoiajsdjflqkuwef','46','request','77','response','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('6','cnjasdfluqhwehlkjqwheaizjdhasd','78','request-response','47','response','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('7','klasehnfkljashdnflhkjahwlekdjf','52','rpcrequest','83','rpcresponse','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('8','kjashdlkfjabsdkfjhbakwjlkhqwjh','82','rpcrequest','53','rpcresponse','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40'),('9','laksjdfkjiqwheiuudiuhvqweqwefa','49','publish','50','subscribe','1','banyanAdmin','AUDIT_SUCCESS','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40','2015-04-28 19:48:40');
/*!40000 ALTER TABLE `SINK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SINK_AUDIT_HISTORY`
--

DROP TABLE IF EXISTS `SINK_AUDIT_HISTORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SINK_AUDIT_HISTORY` (
  `SINK_AUDIT_ID` varchar(20) NOT NULL,
  `SINK_ID` varchar(20) DEFAULT NULL,
  `AUDIT_TYPE_CODE` varchar(100) DEFAULT NULL,
  `AUDITOR` varchar(20) DEFAULT NULL,
  `FROM_DATE` datetime DEFAULT NULL,
  `THRU_DATE` datetime DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime DEFAULT NULL,
  `CREATED_STAMP` datetime DEFAULT NULL,
  `CREATED_TX_STAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`SINK_AUDIT_ID`),
  KEY `fk_SinkAuditHistory_AuditType` (`AUDIT_TYPE_CODE`),
  KEY `fk_SinkAuditHistory_Sink` (`SINK_ID`),
  KEY `fk_SinkAuditHistory_UserLogin` (`AUDITOR`),
  KEY `SNK_ADT_HSR_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  KEY `SNK_ADT_HSR_TXCRTS` (`CREATED_TX_STAMP`),
  CONSTRAINT `fk_SinkAuditHistory_Sink` FOREIGN KEY (`SINK_ID`) REFERENCES `SINK` (`SINK_ID`),
  CONSTRAINT `fk_SinkAuditHistory_AuditType` FOREIGN KEY (`AUDIT_TYPE_CODE`) REFERENCES `AUDIT_TYPE` (`AUDIT_TYPE_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SINK_AUDIT_HISTORY`
--

LOCK TABLES `SINK_AUDIT_HISTORY` WRITE;
/*!40000 ALTER TABLE `SINK_AUDIT_HISTORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `SINK_AUDIT_HISTORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'banyan_DB'
--

--
-- Dumping routines for database 'banyan_DB'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-05-06  9:11:30
