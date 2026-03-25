/*
 Navicat Premium Dump SQL

 Source Server         : mysql-dev
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : 127.0.0.1:13306
 Source Schema         : Travel

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 22/03/2026 15:46:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for scenic_spot
-- ----------------------------
DROP TABLE IF EXISTS `scenic_spot`;
CREATE TABLE `scenic_spot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景区名称',
  `volume` int NOT NULL DEFAULT 0 COMMENT '每日剩余预约名额',
  `free_time` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '每天开放时间说明',
  `rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景区预约规则',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of scenic_spot
-- ----------------------------
INSERT INTO `scenic_spot` VALUES (1, '岳麓山', 985, '旺季6:00-23:00，淡季7:00-22:00', '每人每次最多预约6人，需实名制，当日00:00起可预约3日内门票');
INSERT INTO `scenic_spot` VALUES (2, '橘子洲', 500, '全年07:00-22:00', '最多提前3天预约，每次最多5人');
INSERT INTO `scenic_spot` VALUES (3, '岳阳楼', 300, '夏令7:00-18:30，冬令7:30-18:00', '实行分时预约，现场购票可能无法入园');
INSERT INTO `scenic_spot` VALUES (4, '南岳衡山', 800, '中心景区全天，大庙8:00-17:30', '建议提前预约，节假日限流');
INSERT INTO `scenic_spot` VALUES (5, '天门山', 400, '全年07:30-19:00', '旺季需提前3天预约');
INSERT INTO `scenic_spot` VALUES (6, '韶山', 600, '08:00-17:30', '必须实名制分时段预约，现场不设售票处');
INSERT INTO `scenic_spot` VALUES (7, '张家界', 1200, '景区全天开放', '每日限量5.3万人次，门票4日有效');
INSERT INTO `scenic_spot` VALUES (8, '凤凰古城', 900, '全天开放', '古城免费，内部景点需提前预约');
INSERT INTO `scenic_spot` VALUES (9, '东江湖', 350, '全天开放', '观雾最佳时间6:30-8:00');
INSERT INTO `scenic_spot` VALUES (10, '崀山', 450, '游客中心08:00-17:30', '建议预留1天游览时间');
INSERT INTO `scenic_spot` VALUES (11, '天王山', 1000, '每天早上10:00-17：30', '建议提前一天进行');

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码，加密存储',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称，默认是随机字符',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户头像',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, '18529424417', NULL, 'user_3479996768', '', '2026-03-19 02:34:05', '2026-03-19 02:34:05');

-- ----------------------------
-- Table structure for user_scenic
-- ----------------------------
DROP TABLE IF EXISTS `user_scenic`;
CREATE TABLE `user_scenic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint NOT NULL COMMENT '景区ID',
  `scenic_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景区名称',
  `date` date NOT NULL COMMENT '预约日期',
  `time_slot` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约时间段（上午/下午/晚上）',
  `person_count` int NOT NULL DEFAULT 1 COMMENT '预约人数',
  `contact_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `phone` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_scenic_id`(`scenic_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户景区预约关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_scenic
-- ----------------------------
INSERT INTO `user_scenic` VALUES (1, 1, '岳麓山', '2026-03-21', '上午', 2, '张三', '13800138000', NULL, '岳20260319532629');
INSERT INTO `user_scenic` VALUES (2, 1, '岳麓山', '2026-03-21', '下午', 3, '王五', '13800138000', NULL, '岳20260319874842');
INSERT INTO `user_scenic` VALUES (3, 1, '岳麓山', '2026-03-21', '上午', 2, '张三', '13800138000', NULL, '岳20260319450172');
INSERT INTO `user_scenic` VALUES (4, 1, '岳麓山', '2026-03-22', '上午', 2, '张三', '13800138000', NULL, '岳20260320698872');
INSERT INTO `user_scenic` VALUES (5, 1, '岳麓山', '2026-03-21', '上午', 2, '张三', '13800138000', NULL, '岳20260320563634');
INSERT INTO `user_scenic` VALUES (6, 1, '岳麓山', '2026-03-21', '上午', 2, '张三', '13800138000', NULL, '岳20260320529700');
INSERT INTO `user_scenic` VALUES (7, 1, '岳麓山', '2026-03-21', '上午', 2, '张三', '13800138000', NULL, '岳20260320027404');

SET FOREIGN_KEY_CHECKS = 1;
