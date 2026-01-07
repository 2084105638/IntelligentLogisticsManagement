/*
 Navicat Premium Dump SQL

 Source Server         : project_mysql
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : logistics

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 07/01/2026 09:33:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_car
-- ----------------------------
DROP TABLE IF EXISTS `sys_car`;
CREATE TABLE `sys_car` (
  `car_id` bigint NOT NULL COMMENT '车辆主键 id',
  `location` varchar(255) NOT NULL COMMENT '车辆位置',
  `status` tinyint(1) NOT NULL COMMENT '车辆状态 空闲中:0 使用中:1 维修中:2',
  `driver_id` bigint DEFAULT NULL COMMENT '关联的司机 id',
  PRIMARY KEY (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_consignor
-- ----------------------------
DROP TABLE IF EXISTS `sys_consignor`;
CREATE TABLE `sys_consignor` (
  `consignor_id` bigint NOT NULL COMMENT '货主主键 id',
  `user_id` bigint NOT NULL COMMENT '关联的用户 id',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '手机号',
  `password` varchar(50) NOT NULL COMMENT '密码',
  PRIMARY KEY (`consignor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_dispatcher
-- ----------------------------
DROP TABLE IF EXISTS `sys_dispatcher`;
CREATE TABLE `sys_dispatcher` (
  `dispatcher_id` bigint NOT NULL COMMENT '操作员 id',
  `user_id` bigint NOT NULL COMMENT '关联的用户 id',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `status` tinyint(1) NOT NULL COMMENT '状态 禁用:0 启用:1',
  PRIMARY KEY (`dispatcher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_driver
-- ----------------------------
DROP TABLE IF EXISTS `sys_driver`;
CREATE TABLE `sys_driver` (
  `driver_id` bigint NOT NULL COMMENT '司机主键 id',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户 id',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话号码',
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_location
-- ----------------------------
DROP TABLE IF EXISTS `sys_location`;
CREATE TABLE `sys_location` (
  `location_id` bigint NOT NULL COMMENT '位置主键 id',
  `waybill` bigint DEFAULT NULL COMMENT '关联运单 id',
  `location_info` varchar(255) DEFAULT NULL COMMENT '位置信息',
  `location_date` datetime NOT NULL COMMENT '位置上传时间',
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL COMMENT '用户主键id',
  `type` tinyint(1) DEFAULT NULL COMMENT '用户类型 0:管理员 1:货主 2:调度员 3:司机',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_waybill
-- ----------------------------
DROP TABLE IF EXISTS `sys_waybill`;
CREATE TABLE `sys_waybill` (
  `waybill_id` bigint NOT NULL COMMENT '运单主键 id',
  `waybill_identification` bigint NOT NULL COMMENT '运单标识 id，相同的运单不同的历史具有相同的 id',
  `goods_information` varchar(255) DEFAULT NULL COMMENT '货物信息',
  `start_address` varchar(255) NOT NULL COMMENT '起始地址',
  `end_address` varchar(255) NOT NULL COMMENT '结束地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `expected_time_limit` datetime NOT NULL COMMENT '期望时效',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '费用',
  `status` tinyint(1) DEFAULT NULL COMMENT '状态 待分配:0 已分配:1 运输中: 2 已完成:3',
  `created_consignor` bigint NOT NULL COMMENT '创建运单的货主',
  `receiving_consignor` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收运单的货主',
  `car_id` bigint DEFAULT NULL COMMENT '运输车辆 id',
  `changed` tinyint(1) NOT NULL COMMENT '是否已被修改 未被修改:0 已被修改:1',
  `current_location` varchar(50) DEFAULT NULL COMMENT '当前位置',
  PRIMARY KEY (`waybill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_waybill_assignment
-- ----------------------------
DROP TABLE IF EXISTS `sys_waybill_assignment`;
CREATE TABLE `sys_waybill_assignment` (
  `assignment_id` bigint NOT NULL COMMENT '运单操作 id',
  `operator_id` bigint NOT NULL COMMENT '操作者 id',
  `old_waybill_id` bigint NOT NULL COMMENT '修改前运单的id',
  `new_waybill_id` bigint NOT NULL COMMENT '修改后运单的 id',
  `create_time` datetime NOT NULL COMMENT '执行时间',
  PRIMARY KEY (`assignment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for sys_waybill_exception
-- ----------------------------
DROP TABLE IF EXISTS `sys_waybill_exception`;
CREATE TABLE `sys_waybill_exception` (
  `exception_id` bigint NOT NULL COMMENT '异常主键 id',
  `waybill_id` bigint DEFAULT NULL COMMENT '关联的运单 id',
  `car_id` bigint DEFAULT NULL COMMENT '关联的车辆 id',
  `exception_date` datetime NOT NULL COMMENT '异常时间',
  `description` varchar(255) DEFAULT NULL COMMENT '异常描述',
  PRIMARY KEY (`exception_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
