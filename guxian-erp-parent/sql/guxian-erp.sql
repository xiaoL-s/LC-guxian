/*
 Navicat Premium Data Transfer

 Source Server         : Lu
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : guxian-erp

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 27/09/2026 20:52:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for _customer_follow
-- ----------------------------
DROP TABLE IF EXISTS `_customer_follow`;
CREATE TABLE `_customer_follow`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `customer_id` bigint(0) NOT NULL COMMENT '客户id（关联t_customer）',
  `ollow_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跟进内容',
  `ollow_time` datetime(0) NULL DEFAULT NULL COMMENT '跟进时间',
  `ollow_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跟进人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_id`(`customer_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户跟进记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of _customer_follow
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dict_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dict_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort` int(0) NULL DEFAULT 0,
  `del_flag` tinyint(0) NULL DEFAULT 0,
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `img_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '产品图片URL',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'style_8k_sanjie', '8K大料三节', '1', 1, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (2, 'style_8k_sanjie', '8K极窄三节', '2', 2, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (3, 'style_8k_sanjie', '8K极窄两节', '3', 3, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (4, 'style_8k_sanjie', '8K极窄三节小窗口', '4', 4, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (5, 'style_8k_sanjie', '8K极窄三节-全防护', '5', 5, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (6, 'style_8k_sanjie', '8K极窄两节不要下边框', '6', 6, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (7, 'style_8k_sanjie', '8K极窄四节-4节纱', '7', 7, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (8, 'style_8k_sanjie', '8K极窄两节-全防护', '8', 8, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (9, 'style_8k_sanjie', '8K极窄三节小缺口', '9', 9, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-13 21:33:33', 1, '');
INSERT INTO `sys_dict_data` VALUES (10, 'style_8k_sanjie', '8K大料两节', '10', 10, 0, 1, '2026-09-13 21:33:33', 1, '2026-09-14 22:35:47', 1, '');
INSERT INTO `sys_dict_data` VALUES (13, 'style_kuangzhongkuang', '金刚网框中框', '11', 11, 0, 1, '2026-09-14 22:24:21', 1, '2026-09-14 22:35:35', 1, '');
INSERT INTO `sys_dict_data` VALUES (14, 'style_sanjie', '三界测试', '14', 14, 1, 1, '2026-09-17 22:56:50', 1, '2026-09-17 23:09:10', 1, '');
INSERT INTO `sys_dict_data` VALUES (15, 'style_8k_chaixi', '拆洗测试', '15', 15, 1, 1, '2026-09-17 22:59:54', 1, '2026-09-17 23:09:12', 1, '');
INSERT INTO `sys_dict_data` VALUES (16, 'style_kuangzhongkuang', '测试123', '15', 15, 1, 1, '2026-09-17 23:08:41', 1, '2026-09-26 11:10:28', 1, '');
INSERT INTO `sys_dict_data` VALUES (20, 'style_kuangzhongkuang', '护童-压条款（120款）', '1', 1, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (21, 'style_kuangzhongkuang', '拆洗三推', '2', 2, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (22, 'style_kuangzhongkuang', '拆洗2节', '3', 3, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (23, 'style_kuangzhongkuang', '55成品片纱', '4', 4, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (24, 'style_kuangzhongkuang', '金属漆拆洗三推护童', '5', 5, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (25, 'style_kuangzhongkuang', '40平网折叠门', '6', 6, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (26, 'style_kuangzhongkuang', '68V网折叠纱门', '7', 7, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (27, 'style_kuangzhongkuang', '卷筒隐纱', '8', 8, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (28, 'style_kuangzhongkuang', '拆洗款护童-压条款（110款）', '9', 9, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (29, 'style_kuangzhongkuang', '拆洗款护童-两节加杆', '10', 10, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (30, 'style_kuangzhongkuang', '8K拆洗护童款', '11', 11, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (31, 'style_kuangzhongkuang', '8K拆洗三节', '12', 12, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (32, 'style_kuangzhongkuang', '8K拆洗两节', '13', 13, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (33, 'style_kuangzhongkuang', '8K外装内平开', '14', 14, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (34, 'style_kuangzhongkuang', '8K口袋锁纱窗', '15', 15, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (35, 'style_kuangzhongkuang', '8K拆洗护童款两节', '16', 16, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (36, 'style_kuangzhongkuang', '8K口袋锁纱窗加摇头', '17', 17, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (37, 'style_kuangzhongkuang', '拆洗三推-副本', '18', 18, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (38, 'style_kuangzhongkuang', '8K口袋锁护童款-铝合金款', '19', 19, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (39, 'style_kuangzhongkuang', '8K外装内平开护童-铝合金款', '20', 20, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (40, 'style_kuangzhongkuang', '豪华拆洗三推护童款', '21', 21, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (41, 'style_kuangzhongkuang', '豪华拆洗2节护童款', '22', 22, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (42, 'style_kuangzhongkuang', '豪华拆洗三推', '23', 23, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (43, 'style_kuangzhongkuang', '豪华拆洗2节', '24', 24, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (44, 'style_kuangzhongkuang', '8K口袋锁护童款-不锈钢款', '25', 25, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (45, 'style_kuangzhongkuang', '32小方盒-固定', '26', 26, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (46, 'style_kuangzhongkuang', '32平盒-不锈钢', '27', 27, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (47, 'style_kuangzhongkuang', '32平盒-可拆不锈钢', '28', 28, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (48, 'style_kuangzhongkuang', '32平盒-固定', '29', 29, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (49, 'style_kuangzhongkuang', '32平盒-可拆卸', '30', 30, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (50, 'style_kuangzhongkuang', '8K拆洗护童款-铝合金款', '31', 31, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (51, 'style_kuangzhongkuang', '8K拆洗4节-拆洗4节纱', '32', 32, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (52, 'style_kuangzhongkuang', '8K拆洗护童款-4节纱', '33', 33, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (53, 'style_kuangzhongkuang', '8K口袋锁纱窗加摇头-铝合金款', '34', 34, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (54, 'style_kuangzhongkuang', '32平盒-可拆不锈钢-对开窗', '35', 35, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (55, 'style_kuangzhongkuang', '32小方盒-对开窗', '36', 36, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (56, 'style_kuangzhongkuang', '32平盒-可拆-对开窗', '37', 37, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (57, 'style_kuangzhongkuang', '42大方盒-固定', '38', 38, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (58, 'style_kuangzhongkuang', '豪华断桥拆洗2节', '39', 39, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (59, 'style_kuangzhongkuang', '-副本', '40', 40, 1, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:10:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (60, 'style_kuangzhongkuang', '32平盒-对开窗-固定', '41', 41, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (61, 'style_kuangzhongkuang', '断桥隐纱-带护栏', '42', 42, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (62, 'style_kuangzhongkuang', '断桥隐纱-三大一小 防护栏', '43', 43, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (63, 'style_kuangzhongkuang', '32可拆平盒-8K外宽-无缝焊接', '44', 44, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (64, 'style_kuangzhongkuang', '42大方盒-对开窗', '45', 45, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (65, 'style_kuangzhongkuang', '8K拆洗三节-小窗口', '46', 46, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (66, 'style_kuangzhongkuang', '82纱门单开', '47', 47, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (67, 'style_kuangzhongkuang', '82纱门对开', '48', 48, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (68, 'style_kuangzhongkuang', '52纱门单开', '49', 49, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (69, 'style_kuangzhongkuang', '52纱门对开', '50', 50, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (70, 'style_kuangzhongkuang', '断桥隐纱-三大一小', '51', 51, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (71, 'style_kuangzhongkuang', '8K拆洗护童款-加高款', '52', 52, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (72, 'style_kuangzhongkuang', '8K外装内平开护童款-不锈钢款', '53', 53, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (73, 'style_sanjie', '小料三节', '1', 1, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (74, 'style_sanjie', '折边三节', '2', 2, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (75, 'style_sanjie', '大料三节', '3', 3, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (76, 'style_sanjie', '小料两节', '4', 4, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (77, 'style_sanjie', '折边两节', '5', 5, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (78, 'style_sanjie', '50成品片沙', '6', 6, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (79, 'style_sanjie', '大料两节', '7', 7, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (80, 'style_sanjie', '防护加网', '8', 8, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (81, 'style_sanjie', '小料两节-不要下边框', '9', 9, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (82, 'style_sanjie', '小料三节-可平开可上下开', '10', 10, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (83, 'style_sanjie', '单独防护栏', '11', 11, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (84, 'style_sanjie', '框中框材料单独防护栏', '12', 12, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (85, 'style_sanjie', '加厚双孔断桥防护栏', '13', 13, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (86, 'style_sanjie', '小料三节-小缺口', '14', 14, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (87, 'style_sanjie', '极窄单孔断桥防护栏', '15', 15, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (88, 'style_sanjie', '极窄双孔断桥防护栏', '16', 16, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (89, 'style_sanjie', '加厚单孔断桥防护栏', '17', 17, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (90, 'style_sanjie', '不锈钢59.9款防护栏', '18', 18, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (91, 'style_sanjie', '不锈钢78款防护栏', '19', 19, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (92, 'style_sanjie', '不锈钢极窄款防护栏', '20', 20, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (93, 'style_sanjie', '8K50片沙', '21', 21, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (94, 'style_sanjie', '豪华固定三节', '22', 22, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (95, 'style_sanjie', '豪华固定2节', '23', 23, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (96, 'style_sanjie', '不锈钢88款防护栏-15X15', '24', 24, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (97, 'style_sanjie', '豪华断桥拆洗三推', '25', 25, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (98, 'style_sanjie', '补', '26', 26, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');
INSERT INTO `sys_dict_data` VALUES (99, 'style_sanjie', '8K60片沙', '27', 27, 0, 1, '2026-09-26 11:06:20', 1, '2026-09-26 11:06:20', 1, '');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型编码',
  `dict_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `status` tinyint(0) NULL DEFAULT 1,
  `del_flag` tinyint(0) NULL DEFAULT 0,
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 'style_kuangzhongkuang', '框中框系列', 1, 0, 1, '2026-09-13 21:32:28', 1, '2026-09-13 21:32:28');
INSERT INTO `sys_dict_type` VALUES (2, 'style_sanjie', '三节系列', 1, 0, 1, '2026-09-13 21:32:28', 1, '2026-09-13 21:32:28');
INSERT INTO `sys_dict_type` VALUES (3, 'style_8k_sanjie', '8K三节系列', 1, 0, 1, '2026-09-13 21:32:28', 1, '2026-09-13 21:32:28');
INSERT INTO `sys_dict_type` VALUES (4, 'style_8k_chaixi', '8K拆洗系列', 1, 0, 1, '2026-09-13 21:32:28', 1, '2026-09-13 21:32:28');
INSERT INTO `sys_dict_type` VALUES (7, 'cheshi', '测试', 1, 1, 1, '2026-09-18 09:26:19', 1, '2026-09-18 09:27:29');

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '操作系统',
  `login_status` tinyint(0) NOT NULL COMMENT '登录状态 0失败 1成功',
  `msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE,
  INDEX `idx_login_time`(`login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(0) NULL DEFAULT 0,
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `menu_type` tinyint(0) NULL DEFAULT NULL,
  `sort` int(0) NULL DEFAULT 0,
  `del_flag` tinyint(0) NULL DEFAULT 0,
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_menu_type`(`menu_type`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1011 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '首页看板', '/xiaolu', 'dashboard:view', 1, 1, 0, 'HomeOutlined');
INSERT INTO `sys_menu` VALUES (10, 0, '系统管理', '/system', '', 1, 2, 0, 'SettingOutlined');
INSERT INTO `sys_menu` VALUES (20, 0, '客户管理', '/customer', '', 1, 3, 0, 'UserOutlined');
INSERT INTO `sys_menu` VALUES (30, 0, '供应商管理', '/supplier', '', 1, 4, 0, 'ShopOutlined');
INSERT INTO `sys_menu` VALUES (40, 0, '销售订单管理', '/sales', '', 1, 5, 0, 'ShoppingCartOutlined');
INSERT INTO `sys_menu` VALUES (50, 0, '采购管理', '/purchase', '', 1, 6, 0, 'ShoppingOutlined');
INSERT INTO `sys_menu` VALUES (60, 0, '库存管理', '/stock', '', 1, 7, 0, 'InboxOutlined');
INSERT INTO `sys_menu` VALUES (70, 0, '生产管理', '/production', '', 1, 8, 0, 'ToolOutlined');
INSERT INTO `sys_menu` VALUES (80, 0, '财物管理', '/finance', ' ', 1, 9, 0, 'MoneyCollectOutlined');
INSERT INTO `sys_menu` VALUES (90, 0, '报表统计中心', '/report', '', 1, 10, 0, 'BarChartOutlined');
INSERT INTO `sys_menu` VALUES (100, 0, '小程序配置', '/mini', '', 1, 11, 0, 'WechatOutlined');
INSERT INTO `sys_menu` VALUES (101, 10, '员工管理', '/system/user', 'system:user:list', 2, 1, 0, 'TeamOutlined');
INSERT INTO `sys_menu` VALUES (102, 10, '角色管理', '/system/role', 'system:role:list', 2, 2, 0, 'SafetyOutlined');
INSERT INTO `sys_menu` VALUES (103, 10, '菜单管理', '/system/menu', 'system:menu:list', 2, 3, 0, 'MenuOutlined');
INSERT INTO `sys_menu` VALUES (104, 10, '字典管理', '/system/dict', 'system:dict:list', 2, 4, 0, 'BookOutlined');
INSERT INTO `sys_menu` VALUES (105, 10, '打印模板管理', '/system/printTemplate', 'system:print:list', 2, 5, 0, 'PrinterOutlined');
INSERT INTO `sys_menu` VALUES (106, 10, '操作日志', '/system/operlog', 'system:log:list', 2, 6, 0, 'FileTextOutlined');
INSERT INTO `sys_menu` VALUES (201, 20, '客户档案', '/customer/list', 'customer:list:view', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (202, 20, '客户跟进记录', '/customer/follow', 'customer:follow:view', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (301, 30, '供应商档案', '/supplier/list', 'supplier:list:view', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (302, 30, '供应商报价', '/supplier/price', 'supplier:price:view', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (401, 40, '产品BOM配置', '/sales/productBom', 'sales:bom:view', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (402, 40, '下单', '/sales/order/entry', 'sales:order:add', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (403, 40, '订单列表', '/sales/order/list', 'sales:order:list', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (404, 40, '订单明细', '/sales/order/items', 'sales:order:item', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (405, 40, '订单进度跟踪', '/sales/order/track', 'sales:track:view', 2, 5, 0, NULL);
INSERT INTO `sys_menu` VALUES (501, 50, '采购申请单', '/purchase/apply', 'purchase:apply:list', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (502, 50, '采购订单', '/purchase/order', 'purchase:order:list', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (503, 50, '采购入库', '/purchase/instock', 'purchase:in:list', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (504, 50, '采购退货', '/purchase/return', 'purchase:return:list', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (601, 60, '物料档案', '/stock/material', 'stock:material:list', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (602, 60, '库位货架管理', '/stock/shelf', 'stock:shelf:list', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (603, 60, '出入库单据', '/stock/bill', 'stock:bill:list', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (604, 60, '库存盘点', '/stock/check', 'stock:check:list', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (605, 60, '库存预警', '/stock/warn', 'stock:warn:view', 2, 5, 0, NULL);
INSERT INTO `sys_menu` VALUES (701, 70, '工序管理', '/production/process', 'production:process:list', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (702, 70, '生产工单', '/production/workorder', 'production:workorder:list', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (703, 70, '工人报工记录', '/production/report', 'production:report:list', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (704, 70, '计件工资核算', '/production/wage', 'production:wage:calc', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (801, 80, '应收款管理', '/finance/receivable', 'finance:receivable:list', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (802, 80, '应付款管理', '/finance/payable', 'finance:payable:list', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (803, 80, '订单成本核算', '/finance/cost', 'finance:cost:view', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (804, 80, '费用登记', '/finance/expense', 'finance:expense:list', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (901, 90, '销售报表', '/report/sales', 'report:sales:view', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (902, 90, '生产报表', '/report/production', 'report:production:view', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (903, 90, '库存收发存报表', '/report/stock', 'report:stock:view', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (904, 90, '经营利润报表', '/report/profit', 'report:profit:view', 2, 4, 0, NULL);
INSERT INTO `sys_menu` VALUES (1001, 100, '小程序权限设置', '/mini/role', 'mini:role:config', 2, 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (1002, 100, '消息推送配置', '/mini/msg', 'mini:msg:config', 2, 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (1003, 100, '同步日志', ' ', 'mini:log:view', 2, 3, 0, NULL);
INSERT INTO `sys_menu` VALUES (1007, 100, '小卢官方', 'xiaoluguangfang', NULL, NULL, 0, 0, '');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oper_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `oper_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `oper_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `oper_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1, 'admin', '超级管理员', '用户管理', '新增', '新增用户【文员】', '127.0.0.1', '2026-09-16 08:30:00');
INSERT INTO `sys_oper_log` VALUES (2, 'admin', '超级管理员', '角色管理', '修改', '修改角色【文员】权限', '127.0.0.1', '2026-09-17 08:32:00');
INSERT INTO `sys_oper_log` VALUES (3, 'zhangsan', '张三', '字典管理', '删除', '删除字典类型【款式】', '192.168.1.100', '2026-09-17 09:10:00');
INSERT INTO `sys_oper_log` VALUES (4, 'lisi', '李四', '打印模板管理', '新增', '新增销售订单打印模板', '192.168.1.101', '2026-09-17 09:20:00');
INSERT INTO `sys_oper_log` VALUES (5, NULL, NULL, '字典管理', '保存', '新增或修改字典信息', '127.0.0.1', '2026-09-17 22:56:50');
INSERT INTO `sys_oper_log` VALUES (6, NULL, NULL, '字典管理', '保存', '新增或修改字典信息', '127.0.0.1', '2026-09-17 22:59:54');
INSERT INTO `sys_oper_log` VALUES (7, '卢总', '卢超', '字典管理', '保存', '新增或修改字典信息', '127.0.0.1', '2026-09-17 23:08:41');
INSERT INTO `sys_oper_log` VALUES (8, '卢总', '卢超', '字典管理', '删除', '删除字典', '127.0.0.1', '2026-09-17 23:09:10');
INSERT INTO `sys_oper_log` VALUES (9, '卢总', '卢超', '字典管理', '删除', '删除字典', '127.0.0.1', '2026-09-17 23:09:12');
INSERT INTO `sys_oper_log` VALUES (10, '卢总', '卢超', '员工管理', '保存', '新增/修改员工信息', '127.0.0.1', '2026-09-17 23:20:24');
INSERT INTO `sys_oper_log` VALUES (11, '卢总', '卢超', '员工管理', '删除', '删除员工', '127.0.0.1', '2026-09-17 23:20:32');
INSERT INTO `sys_oper_log` VALUES (12, '卢总', '卢超', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-17 23:21:31');
INSERT INTO `sys_oper_log` VALUES (13, '卢总', '卢超', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-17 23:21:54');
INSERT INTO `sys_oper_log` VALUES (14, '卢总', '卢超', '菜单管理', '保存', '新增/修改菜单信息', '127.0.0.1', '2026-09-17 23:22:32');
INSERT INTO `sys_oper_log` VALUES (15, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-17 23:23:04');
INSERT INTO `sys_oper_log` VALUES (16, '卢总', '卢超', '字典管理（类型）', '保存', '新增或修改字典类型信息', '127.0.0.1', '2026-09-18 09:26:19');
INSERT INTO `sys_oper_log` VALUES (17, '卢总', '卢超', '字典管理（类型）', '删除', '删除字典类型', '127.0.0.1', '2026-09-18 09:27:29');
INSERT INTO `sys_oper_log` VALUES (18, '卢总', '卢超', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-18 17:02:29');
INSERT INTO `sys_oper_log` VALUES (19, '卢总', '卢超', '客户管理', '删除', '删除客户', '127.0.0.1', '2026-09-18 17:03:16');
INSERT INTO `sys_oper_log` VALUES (20, '卢总', '卢超', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-18 17:09:57');
INSERT INTO `sys_oper_log` VALUES (21, '卢总', '卢超', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-18 21:35:05');
INSERT INTO `sys_oper_log` VALUES (22, '卢总', '卢超', '客户跟进记录', '新增/编辑', '保存客户跟进记录', '127.0.0.1', '2026-09-18 21:55:00');
INSERT INTO `sys_oper_log` VALUES (23, '卢总', '卢超', '客户跟进记录', '新增/编辑', '保存客户跟进记录', '127.0.0.1', '2026-09-18 21:57:42');
INSERT INTO `sys_oper_log` VALUES (24, '卢总', '卢超', '客户跟进记录', '新增/编辑', '保存客户跟进记录', '127.0.0.1', '2026-09-18 22:02:09');
INSERT INTO `sys_oper_log` VALUES (25, '卢总', '卢超', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 08:56:13');
INSERT INTO `sys_oper_log` VALUES (39, 'clerk', '文员小张', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-19 10:38:12');
INSERT INTO `sys_oper_log` VALUES (40, 'clerk', '文员小张', '销售订单', '受理', '受理销售订单', '127.0.0.1', '2026-09-19 10:38:12');
INSERT INTO `sys_oper_log` VALUES (41, 'clerk', '文员小张', '销售订单', '收款', '订单收款登记', '127.0.0.1', '2026-09-19 10:38:13');
INSERT INTO `sys_oper_log` VALUES (42, 'clerk', '文员小张', '销售订单', '生产', '订单开工（预留）', '127.0.0.1', '2026-09-19 10:38:19');
INSERT INTO `sys_oper_log` VALUES (43, 'clerk', '文员小张', '销售订单', '生产', '订单完工（预留）', '127.0.0.1', '2026-09-19 10:38:19');
INSERT INTO `sys_oper_log` VALUES (44, 'clerk', '文员小张', '销售订单', '发货', '订单发货', '127.0.0.1', '2026-09-19 10:38:19');
INSERT INTO `sys_oper_log` VALUES (45, 'clerk', '文员小张', '销售订单', '完成', '订单完成确认', '127.0.0.1', '2026-09-19 10:38:19');
INSERT INTO `sys_oper_log` VALUES (46, 'clerk', '文员小张', '销售订单', '收款', '订单结清', '127.0.0.1', '2026-09-19 10:38:19');
INSERT INTO `sys_oper_log` VALUES (47, 'clerk', '文员小张', '员工管理', '保存', '新增/修改员工信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (48, 'clerk', '文员小张', '员工管理', '删除', '删除员工', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (49, 'clerk', '文员小张', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (50, 'clerk', '文员小张', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (51, 'clerk', '文员小张', '角色管理', '删除', '删除角色', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (52, 'clerk', '文员小张', '菜单管理', '保存', '新增/修改菜单信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (53, 'clerk', '文员小张', '字典管理（类型）', '保存', '新增或修改字典类型信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (54, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:51:00');
INSERT INTO `sys_oper_log` VALUES (55, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (56, 'clerk', '文员小张', '字典管理（数据）', '删除', '删除字典数据', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (57, 'clerk', '文员小张', '字典管理（类型）', '删除', '删除字典类型', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (58, 'clerk', '文员小张', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (59, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (60, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (61, 'clerk', '文员小张', '产品档案', '保存', '新增/编辑产品', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (62, 'clerk', '文员小张', '产品档案', '删除', '删除产品', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (63, 'clerk', '文员小张', '销售订单', '删除', '删除销售订单', '127.0.0.1', '2026-09-19 10:51:01');
INSERT INTO `sys_oper_log` VALUES (64, 'clerk', '文员小张', '菜单管理', '保存', '新增/修改菜单信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (65, 'clerk', '文员小张', '菜单管理', '删除', '删除菜单', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (66, 'clerk', '文员小张', '字典管理（类型）', '保存', '新增或修改字典类型信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (67, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (68, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (69, 'clerk', '文员小张', '字典管理（数据）', '删除', '删除字典数据', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (70, 'clerk', '文员小张', '字典管理（类型）', '删除', '删除字典类型', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (71, 'clerk', '文员小张', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (72, 'clerk', '文员小张', '打印模板管理', '保存', '启用/禁用打印模板信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (73, 'clerk', '文员小张', '打印模板管理', '保存', '启用/禁用打印模板信息', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (74, 'clerk', '文员小张', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-19 10:53:30');
INSERT INTO `sys_oper_log` VALUES (75, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:53:31');
INSERT INTO `sys_oper_log` VALUES (76, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:53:31');
INSERT INTO `sys_oper_log` VALUES (77, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:53:31');
INSERT INTO `sys_oper_log` VALUES (78, 'clerk', '文员小张', '客户跟进记录', '新增/编辑', '保存客户跟进记录', '127.0.0.1', '2026-09-19 10:53:31');
INSERT INTO `sys_oper_log` VALUES (79, 'clerk', '文员小张', '客户管理', '批量删除', '批量删除客户', '127.0.0.1', '2026-09-19 10:53:31');
INSERT INTO `sys_oper_log` VALUES (80, 'clerk', '文员小张', '产品BOM', '保存', '配置产品BOM物料清单', '127.0.0.1', '2026-09-19 10:53:32');
INSERT INTO `sys_oper_log` VALUES (81, 'clerk', '文员小张', '产品档案', '删除', '删除产品', '127.0.0.1', '2026-09-19 10:53:32');
INSERT INTO `sys_oper_log` VALUES (82, 'clerk', '文员小张', '销售订单', '删除', '删除销售订单', '127.0.0.1', '2026-09-19 10:53:32');
INSERT INTO `sys_oper_log` VALUES (83, 'clerk', '文员小张', '员工管理', '保存', '新增/修改员工信息', '127.0.0.1', '2026-09-19 10:57:12');
INSERT INTO `sys_oper_log` VALUES (84, 'clerk', '文员小张', '员工管理', '删除', '删除员工', '127.0.0.1', '2026-09-19 10:57:12');
INSERT INTO `sys_oper_log` VALUES (85, 'clerk', '文员小张', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-19 10:57:12');
INSERT INTO `sys_oper_log` VALUES (86, 'clerk', '文员小张', '角色管理', '保存', '新增/修改角色信息', '127.0.0.1', '2026-09-19 10:57:12');
INSERT INTO `sys_oper_log` VALUES (87, 'clerk', '文员小张', '角色管理', '删除', '删除角色', '127.0.0.1', '2026-09-19 10:57:12');
INSERT INTO `sys_oper_log` VALUES (88, 'clerk', '文员小张', '菜单管理', '保存', '新增/修改菜单信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (89, 'clerk', '文员小张', '菜单管理', '删除', '删除菜单', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (90, 'clerk', '文员小张', '字典管理（类型）', '保存', '新增或修改字典类型信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (91, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (92, 'clerk', '文员小张', '字典管理（数据）', '保存', '新增或修改字典数据信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (93, 'clerk', '文员小张', '字典管理（数据）', '删除', '删除字典数据', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (94, 'clerk', '文员小张', '字典管理（类型）', '删除', '删除字典类型', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (95, 'clerk', '文员小张', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (96, 'clerk', '文员小张', '打印模板管理', '保存', '启用/禁用打印模板信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (97, 'clerk', '文员小张', '打印模板管理', '保存', '启用/禁用打印模板信息', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (98, 'clerk', '文员小张', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-19 10:57:13');
INSERT INTO `sys_oper_log` VALUES (99, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (100, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (101, 'clerk', '文员小张', '客户管理', '保存', '新增/编辑客户信息', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (102, 'clerk', '文员小张', '客户跟进记录', '新增/编辑', '保存客户跟进记录', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (103, 'clerk', '文员小张', '客户跟进记录', '删除', '删除客户跟进记录', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (104, 'clerk', '文员小张', '客户管理', '批量删除', '批量删除客户', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (105, 'clerk', '文员小张', '产品档案', '保存', '新增/编辑产品', '127.0.0.1', '2026-09-19 10:57:14');
INSERT INTO `sys_oper_log` VALUES (106, 'clerk', '文员小张', '产品BOM', '保存', '配置产品BOM物料清单', '127.0.0.1', '2026-09-19 10:57:15');
INSERT INTO `sys_oper_log` VALUES (107, 'clerk', '文员小张', '产品档案', '删除', '删除产品', '127.0.0.1', '2026-09-19 10:57:15');
INSERT INTO `sys_oper_log` VALUES (108, 'clerk', '文员小张', '销售订单', '删除', '删除销售订单', '127.0.0.1', '2026-09-19 10:57:15');
INSERT INTO `sys_oper_log` VALUES (109, '卢总', '卢超', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-19 11:10:07');
INSERT INTO `sys_oper_log` VALUES (110, '卢总', '卢超', '产品档案', '保存', '新增/编辑产品', '127.0.0.1', '2026-09-19 11:13:48');
INSERT INTO `sys_oper_log` VALUES (111, '卢总', '卢超', '产品BOM', '保存', '配置产品BOM物料清单', '127.0.0.1', '2026-09-19 11:14:04');
INSERT INTO `sys_oper_log` VALUES (112, 'clerk', '文员小张', '销售订单', '受理', '受理销售订单', '127.0.0.1', '2026-09-19 11:18:04');
INSERT INTO `sys_oper_log` VALUES (113, '卢总', '卢超', '生产工单', '', '拆单生成工单', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (114, '卢总', '卢超', '生产工单', '', '工单领料', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (115, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (116, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (117, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (118, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:43');
INSERT INTO `sys_oper_log` VALUES (119, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:44');
INSERT INTO `sys_oper_log` VALUES (120, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:44');
INSERT INTO `sys_oper_log` VALUES (121, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:11:44');
INSERT INTO `sys_oper_log` VALUES (122, '卢总', '卢超', '生产工单', '', '完工入库', '127.0.0.1', '2026-09-22 23:11:44');
INSERT INTO `sys_oper_log` VALUES (123, '卢总', '卢超', '出入库单据', '', '创建出入库单据', '127.0.0.1', '2026-09-22 23:25:35');
INSERT INTO `sys_oper_log` VALUES (124, '卢总', '卢超', '库存盘点', '', '创建盘点单', '127.0.0.1', '2026-09-22 23:25:54');
INSERT INTO `sys_oper_log` VALUES (125, '卢总', '卢超', '库存盘点', '', '录入实盘数', '127.0.0.1', '2026-09-22 23:25:54');
INSERT INTO `sys_oper_log` VALUES (126, '卢总', '卢超', '库存盘点', '', '录入实盘数', '127.0.0.1', '2026-09-22 23:26:08');
INSERT INTO `sys_oper_log` VALUES (127, '卢总', '卢超', '库存盘点', '', '盘点过账', '127.0.0.1', '2026-09-22 23:26:08');
INSERT INTO `sys_oper_log` VALUES (128, '卢总', '卢超', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-22 23:38:06');
INSERT INTO `sys_oper_log` VALUES (129, '卢总', '卢超', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-22 23:39:05');
INSERT INTO `sys_oper_log` VALUES (130, '卢总', '卢超', '销售订单', '受理', '受理销售订单', '127.0.0.1', '2026-09-22 23:39:26');
INSERT INTO `sys_oper_log` VALUES (131, '卢总', '卢超', '销售订单', '生产', '订单开工（预留）', '127.0.0.1', '2026-09-22 23:41:08');
INSERT INTO `sys_oper_log` VALUES (132, '卢总', '卢超', '销售订单', '发货', '订单发货', '127.0.0.1', '2026-09-22 23:41:40');
INSERT INTO `sys_oper_log` VALUES (133, '卢总', '卢超', '生产工单', '', '拆单生成工单', '127.0.0.1', '2026-09-22 23:53:07');
INSERT INTO `sys_oper_log` VALUES (134, '卢总', '卢超', '销售订单', '生产', '订单开工（预留）', '127.0.0.1', '2026-09-22 23:53:07');
INSERT INTO `sys_oper_log` VALUES (135, '卢总', '卢超', '生产工单', '', '工单领料', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (136, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (137, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (138, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (139, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (140, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (141, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (142, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-22 23:53:51');
INSERT INTO `sys_oper_log` VALUES (143, '卢总', '卢超', '生产工单', '', '完工入库', '127.0.0.1', '2026-09-22 23:53:52');
INSERT INTO `sys_oper_log` VALUES (144, '卢总', '卢超', '销售订单', '发货', '订单发货', '127.0.0.1', '2026-09-22 23:53:52');
INSERT INTO `sys_oper_log` VALUES (145, '卢总', '卢超', '销售订单', '完成', '订单完成确认', '127.0.0.1', '2026-09-22 23:53:52');
INSERT INTO `sys_oper_log` VALUES (146, '卢总', '卢超', '销售订单', '收款', '订单收款登记', '127.0.0.1', '2026-09-22 23:55:55');
INSERT INTO `sys_oper_log` VALUES (147, '卢总', '卢超', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-22 23:58:12');
INSERT INTO `sys_oper_log` VALUES (148, '卢总', '卢超', '销售订单', '受理', '受理销售订单', '127.0.0.1', '2026-09-22 23:58:32');
INSERT INTO `sys_oper_log` VALUES (149, '卢总', '卢超', '生产工单', '', '拆单生成工单', '127.0.0.1', '2026-09-22 23:59:04');
INSERT INTO `sys_oper_log` VALUES (150, '卢总', '卢超', '销售订单', '生产', '订单开工（预留）', '127.0.0.1', '2026-09-22 23:59:04');
INSERT INTO `sys_oper_log` VALUES (151, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-24 13:38:51');
INSERT INTO `sys_oper_log` VALUES (152, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-24 13:45:19');
INSERT INTO `sys_oper_log` VALUES (153, '卢总', '卢超', '销售订单', '完成', '订单完成确认', '127.0.0.1', '2026-09-24 13:48:24');
INSERT INTO `sys_oper_log` VALUES (154, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 13:58:10');
INSERT INTO `sys_oper_log` VALUES (155, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 13:58:10');
INSERT INTO `sys_oper_log` VALUES (156, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 13:58:10');
INSERT INTO `sys_oper_log` VALUES (157, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 13:58:10');
INSERT INTO `sys_oper_log` VALUES (158, '卢总', '卢超', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 13:58:10');
INSERT INTO `sys_oper_log` VALUES (159, '卢总', '卢超', '销售订单', '保存', '新增/编辑销售订单', '127.0.0.1', '2026-09-24 14:01:34');
INSERT INTO `sys_oper_log` VALUES (160, '卢总', '卢超', '销售订单', '受理', '受理销售订单', '127.0.0.1', '2026-09-24 14:02:08');
INSERT INTO `sys_oper_log` VALUES (161, '胡忠平', '胡忠平', '生产报工', '', '工序报工计件', '127.0.0.1', '2026-09-24 14:35:17');
INSERT INTO `sys_oper_log` VALUES (162, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 12:21:11');
INSERT INTO `sys_oper_log` VALUES (163, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 12:32:05');
INSERT INTO `sys_oper_log` VALUES (164, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 12:33:13');
INSERT INTO `sys_oper_log` VALUES (165, '卢总', '卢超', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 12:51:06');
INSERT INTO `sys_oper_log` VALUES (166, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 12:52:16');
INSERT INTO `sys_oper_log` VALUES (167, '卢总', '卢超', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 12:55:17');
INSERT INTO `sys_oper_log` VALUES (168, '卢总', '卢超', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 12:55:19');
INSERT INTO `sys_oper_log` VALUES (169, '卢总', '卢超', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 12:55:20');
INSERT INTO `sys_oper_log` VALUES (170, 'clerk', '文员小张', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:43:49');
INSERT INTO `sys_oper_log` VALUES (171, 'clerk', '文员小张', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:43:49');
INSERT INTO `sys_oper_log` VALUES (172, 'clerk', '文员小张', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 13:45:26');
INSERT INTO `sys_oper_log` VALUES (173, 'clerk', '文员小张', '打印模板管理', '删除', '删除打印模板', '127.0.0.1', '2026-09-25 13:45:26');
INSERT INTO `sys_oper_log` VALUES (174, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:55:36');
INSERT INTO `sys_oper_log` VALUES (175, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:55:40');
INSERT INTO `sys_oper_log` VALUES (176, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:55:51');
INSERT INTO `sys_oper_log` VALUES (177, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:55:57');
INSERT INTO `sys_oper_log` VALUES (178, '卢总', '卢超', '打印模板管理', '保存', '新增/修改打印模板信息', '127.0.0.1', '2026-09-25 13:56:22');
INSERT INTO `sys_oper_log` VALUES (179, '卢总', '卢超', '字典管理（数据）', '删除', '删除字典数据', '127.0.0.1', '2026-09-26 11:10:20');
INSERT INTO `sys_oper_log` VALUES (180, '卢总', '卢超', '字典管理（数据）', '删除', '删除字典数据', '127.0.0.1', '2026-09-26 11:10:28');
INSERT INTO `sys_oper_log` VALUES (181, '卢总', '卢超', '产品BOM', '保存', '配置产品BOM物料清单', '127.0.0.1', '2026-09-26 14:58:45');
INSERT INTO `sys_oper_log` VALUES (182, '卢总', '卢超', '销售订单', '收款', '订单收款登记', '127.0.0.1', '2026-09-26 17:48:32');
INSERT INTO `sys_oper_log` VALUES (183, '卢总', '卢超', '销售订单', '收款', '订单收款登记', '127.0.0.1', '2026-09-26 17:49:43');
INSERT INTO `sys_oper_log` VALUES (184, '卢总', '卢超', '销售订单', '收款', '订单收款登记', '127.0.0.1', '2026-09-26 17:49:47');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色主键',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 11:48:00', 0);
INSERT INTO `sys_role` VALUES (2, '文员', 'clerk', 2, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 16:46:09', 0);
INSERT INTO `sys_role` VALUES (3, '开料师傅', 'cut_work', 3, NULL, '2026-07-31 10:57:13', NULL, '2026-09-08 11:59:40', 1);
INSERT INTO `sys_role` VALUES (4, '组装师傅', 'assemble_work', 4, NULL, '2026-07-31 10:57:13', NULL, '2026-09-08 11:59:40', 1);
INSERT INTO `sys_role` VALUES (5, '质检师傅', 'inspect_work', 5, NULL, '2026-07-31 10:57:13', NULL, '2026-09-08 11:59:40', 1);
INSERT INTO `sys_role` VALUES (6, '打包师傅', 'package_work', 6, NULL, '2026-07-31 10:57:13', NULL, '2026-09-08 11:59:40', 1);
INSERT INTO `sys_role` VALUES (7, '生产工人', 'worker', 3, NULL, '2026-09-08 11:59:44', 1, '2026-09-12 16:44:59', 0);
INSERT INTO `sys_role` VALUES (8, '生产主管', 'prod_manager', 4, NULL, '2026-09-08 11:59:44', NULL, '2026-09-08 11:59:44', 0);
INSERT INTO `sys_role` VALUES (9, '财务', 'finance', 5, NULL, '2026-09-08 11:59:44', 1, '2026-09-12 16:45:16', 0);
INSERT INTO `sys_role` VALUES (10, '工厂老板', 'boss', 6, NULL, '2026-09-08 11:59:44', 1, '2026-09-12 11:49:23', 0);
INSERT INTO `sys_role` VALUES (13, '小白booss', 'booss', 666, 1, '2026-09-11 22:09:49', 1, '2026-09-12 16:47:08', 1);
INSERT INTO `sys_role` VALUES (14, '合作方', 'hezuofang', 0, 1, '2026-09-12 16:49:42', 1, '2026-09-17 23:21:53', 0);
INSERT INTO `sys_role` VALUES (15, '中介', 'zhongjie', 1, 1, '2026-09-17 23:21:31', 1, '2026-09-17 23:21:31', 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(0) NOT NULL COMMENT '角色ID sys_role.id',
  `menu_id` bigint(0) NOT NULL COMMENT '菜单ID sys_menu.id',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 10);
INSERT INTO `sys_role_menu` VALUES (2, 10);
INSERT INTO `sys_role_menu` VALUES (1, 20);
INSERT INTO `sys_role_menu` VALUES (2, 20);
INSERT INTO `sys_role_menu` VALUES (15, 20);
INSERT INTO `sys_role_menu` VALUES (1, 30);
INSERT INTO `sys_role_menu` VALUES (2, 30);
INSERT INTO `sys_role_menu` VALUES (15, 30);
INSERT INTO `sys_role_menu` VALUES (1, 40);
INSERT INTO `sys_role_menu` VALUES (15, 40);
INSERT INTO `sys_role_menu` VALUES (1, 50);
INSERT INTO `sys_role_menu` VALUES (2, 50);
INSERT INTO `sys_role_menu` VALUES (1, 60);
INSERT INTO `sys_role_menu` VALUES (2, 60);
INSERT INTO `sys_role_menu` VALUES (1, 70);
INSERT INTO `sys_role_menu` VALUES (7, 70);
INSERT INTO `sys_role_menu` VALUES (1, 80);
INSERT INTO `sys_role_menu` VALUES (9, 80);
INSERT INTO `sys_role_menu` VALUES (1, 90);
INSERT INTO `sys_role_menu` VALUES (1, 100);
INSERT INTO `sys_role_menu` VALUES (1, 101);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (1, 102);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (1, 103);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (1, 104);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (1, 105);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (1, 106);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (1, 201);
INSERT INTO `sys_role_menu` VALUES (2, 201);
INSERT INTO `sys_role_menu` VALUES (15, 201);
INSERT INTO `sys_role_menu` VALUES (1, 202);
INSERT INTO `sys_role_menu` VALUES (2, 202);
INSERT INTO `sys_role_menu` VALUES (15, 202);
INSERT INTO `sys_role_menu` VALUES (1, 301);
INSERT INTO `sys_role_menu` VALUES (2, 301);
INSERT INTO `sys_role_menu` VALUES (15, 301);
INSERT INTO `sys_role_menu` VALUES (1, 302);
INSERT INTO `sys_role_menu` VALUES (2, 302);
INSERT INTO `sys_role_menu` VALUES (15, 302);
INSERT INTO `sys_role_menu` VALUES (1, 401);
INSERT INTO `sys_role_menu` VALUES (2, 401);
INSERT INTO `sys_role_menu` VALUES (15, 401);
INSERT INTO `sys_role_menu` VALUES (1, 402);
INSERT INTO `sys_role_menu` VALUES (2, 402);
INSERT INTO `sys_role_menu` VALUES (15, 402);
INSERT INTO `sys_role_menu` VALUES (1, 403);
INSERT INTO `sys_role_menu` VALUES (2, 403);
INSERT INTO `sys_role_menu` VALUES (15, 403);
INSERT INTO `sys_role_menu` VALUES (1, 404);
INSERT INTO `sys_role_menu` VALUES (2, 404);
INSERT INTO `sys_role_menu` VALUES (15, 404);
INSERT INTO `sys_role_menu` VALUES (1, 405);
INSERT INTO `sys_role_menu` VALUES (2, 405);
INSERT INTO `sys_role_menu` VALUES (15, 405);
INSERT INTO `sys_role_menu` VALUES (1, 501);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (1, 502);
INSERT INTO `sys_role_menu` VALUES (2, 502);
INSERT INTO `sys_role_menu` VALUES (1, 503);
INSERT INTO `sys_role_menu` VALUES (2, 503);
INSERT INTO `sys_role_menu` VALUES (1, 504);
INSERT INTO `sys_role_menu` VALUES (2, 504);
INSERT INTO `sys_role_menu` VALUES (1, 601);
INSERT INTO `sys_role_menu` VALUES (2, 601);
INSERT INTO `sys_role_menu` VALUES (1, 602);
INSERT INTO `sys_role_menu` VALUES (2, 602);
INSERT INTO `sys_role_menu` VALUES (1, 603);
INSERT INTO `sys_role_menu` VALUES (2, 603);
INSERT INTO `sys_role_menu` VALUES (1, 604);
INSERT INTO `sys_role_menu` VALUES (2, 604);
INSERT INTO `sys_role_menu` VALUES (1, 605);
INSERT INTO `sys_role_menu` VALUES (2, 605);
INSERT INTO `sys_role_menu` VALUES (1, 701);
INSERT INTO `sys_role_menu` VALUES (7, 701);
INSERT INTO `sys_role_menu` VALUES (1, 702);
INSERT INTO `sys_role_menu` VALUES (7, 702);
INSERT INTO `sys_role_menu` VALUES (1, 703);
INSERT INTO `sys_role_menu` VALUES (7, 703);
INSERT INTO `sys_role_menu` VALUES (1, 704);
INSERT INTO `sys_role_menu` VALUES (7, 704);
INSERT INTO `sys_role_menu` VALUES (1, 801);
INSERT INTO `sys_role_menu` VALUES (9, 801);
INSERT INTO `sys_role_menu` VALUES (1, 802);
INSERT INTO `sys_role_menu` VALUES (9, 802);
INSERT INTO `sys_role_menu` VALUES (1, 803);
INSERT INTO `sys_role_menu` VALUES (9, 803);
INSERT INTO `sys_role_menu` VALUES (1, 804);
INSERT INTO `sys_role_menu` VALUES (9, 804);
INSERT INTO `sys_role_menu` VALUES (1, 901);
INSERT INTO `sys_role_menu` VALUES (1, 902);
INSERT INTO `sys_role_menu` VALUES (1, 903);
INSERT INTO `sys_role_menu` VALUES (1, 904);
INSERT INTO `sys_role_menu` VALUES (1, 1001);
INSERT INTO `sys_role_menu` VALUES (1, 1002);
INSERT INTO `sys_role_menu` VALUES (1, 1003);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码 BCrypt',
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '手机号',
  `post_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '岗位类型',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  `roles` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_del`(`del_flag`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '卢总', '124300', '卢超', '15898405119', '', 1, 1, '2026-09-12 11:00:30', 1, '2026-09-12 11:58:26', 0, NULL);
INSERT INTO `sys_user` VALUES (2, 'clerk', '123456', '文员小张', '13800000001', 'clerk', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 11:41:14', 0, NULL);
INSERT INTO `sys_user` VALUES (3, 'cut01', '123456', '开料老王', '13800000002', 'cut', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 16:46:24', 1, NULL);
INSERT INTO `sys_user` VALUES (4, 'assemble01', '123456', '组装小李', '13800000003', 'assemble', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 16:46:28', 1, NULL);
INSERT INTO `sys_user` VALUES (5, 'inspect01', '123456', '质检阿美', '13800000004', 'inspect', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 16:46:31', 1, NULL);
INSERT INTO `sys_user` VALUES (6, 'package01', '123456', '打包阿强', '13800000005', 'package', 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-12 16:46:33', 1, NULL);
INSERT INTO `sys_user` VALUES (7, 'text1', '$2a$10$RudVuQ/j6Taqurdwn8eLse6bu77l3MCl8jkqvGxfyq2DxxHd5ZnXK', '测试', '13800000006', 'cs', 0, NULL, '2026-09-09 22:16:28', 1, '2026-09-19 10:56:17', 0, NULL);
INSERT INTO `sys_user` VALUES (8, 'cs2', '123456', '测试2', '13800000007', '', 0, 1, '2026-09-09 23:01:17', 1, '2026-09-17 23:20:32', 1, NULL);
INSERT INTO `sys_user` VALUES (10, '赵庄钮', '17616669735', '赵庄钮', '17616669735', '工人', 1, NULL, '2026-09-10 00:00:00', NULL, '2026-09-10 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (11, '秦', '18692710618', '秦', '15963254698', '老板', 1, NULL, '2026-07-30 00:00:00', 28, '2026-09-12 12:08:05', 0, '生产,成品仓库');
INSERT INTO `sys_user` VALUES (12, '胡忠平', '15699487310', '胡忠平', '15616994873', '工人', 1, NULL, '2026-07-06 00:00:00', NULL, '2026-07-06 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (13, '李雨冰', '13687311775', '李雨冰', '13687311775', '工人', 1, NULL, '2026-05-03 00:00:00', NULL, '2026-05-03 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (14, '郭威', '17752841285', '郭威', '17752841285', '工人', 1, NULL, '2026-05-03 00:00:00', NULL, '2026-05-03 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (15, '萧伟强', '13433138507', '萧伟强', '13433138507', '工人', 1, NULL, '2026-04-21 00:00:00', NULL, '2026-04-21 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (16, '皮顺和', '15084964215', '皮顺和', '15084964215', '工人', 1, NULL, '2026-04-09 00:00:00', NULL, '2026-04-09 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (17, '雷元凤', '13129485145', '雷元凤', '13129485145', '工人', 1, NULL, '2026-04-08 00:00:00', NULL, '2026-04-08 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (18, '刘范鹏', '13217378336', '刘范鹏', '13217378336', '工人', 1, NULL, '2026-04-08 00:00:00', NULL, '2026-04-08 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (19, '夏洋', '19083786391', '夏洋', '19083786391', '工人', 1, NULL, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (20, '曾向阳', '18153733987', '曾向阳', '18153733987', '工人', 1, NULL, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (21, '刘流荣', '18229992068', '刘流荣', '18229992068', '工人', 1, NULL, '2026-03-25 00:00:00', NULL, '2026-03-25 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (22, '黎万良', '13618493849', '黎万良', '13618493849', '工人', 1, NULL, '2026-03-19 00:00:00', NULL, '2026-03-19 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (23, '李进锋', '18786766112', '李进锋', '18786766112', '工人', 1, NULL, '2026-03-11 00:00:00', NULL, '2026-03-11 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (24, '刘凤姣', '19090702691', '刘凤姣', '19090702691', '工人', 1, NULL, '2026-03-09 00:00:00', NULL, '2026-03-09 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (25, '吴韬', '18908498258', '吴韬', '18908498258', '工人', 1, NULL, '2026-03-07 00:00:00', NULL, '2026-03-07 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (26, '孟苏智', '17336612357', '孟苏智', '17336612357', '工人', 1, NULL, '2026-03-04 00:00:00', NULL, '2026-03-04 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (27, '皮腾龙', '13164760441', '皮腾龙', '13164760441', '工人', 1, NULL, '2026-03-04 00:00:00', NULL, '2026-03-04 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (28, '秦彪', '18692772200', '秦彪', '18692772200', '工人', 1, NULL, '2026-01-16 00:00:00', NULL, '2026-01-16 00:00:00', 0, '生产');
INSERT INTO `sys_user` VALUES (29, '黄新平', '15897337632', '黄新平', '15897337632', '工人', 1, NULL, '2025-12-13 00:00:00', NULL, '2025-12-13 00:00:00', 0, '员工,生产');
INSERT INTO `sys_user` VALUES (30, '杨明龙', '15074325719', '杨明龙', '15074325719', '工人', 1, NULL, '2025-12-12 00:00:00', NULL, '2026-09-12 12:07:05', 0, '生产');
INSERT INTO `sys_user` VALUES (31, '杨柳', '15116150299', '杨柳', '15116150299', '工人', 1, NULL, '2025-12-10 00:00:00', NULL, '2026-09-12 12:07:07', 0, '员工,生产');
INSERT INTO `sys_user` VALUES (32, '李聪', '13677415221', '李聪', '13677415221', '工人', 1, NULL, '2025-12-10 00:00:00', NULL, '2026-09-12 12:07:10', 0, '生产');
INSERT INTO `sys_user` VALUES (33, '胡志勇', '13487503756', '胡志勇', '13487503756', '工人', 1, NULL, '2025-11-26 00:00:00', NULL, '2026-09-12 12:07:12', 0, '生产');
INSERT INTO `sys_user` VALUES (34, '胡家朋', '18573123313', '胡家朋', '18573123313', '工人', 1, NULL, '2025-11-22 00:00:00', NULL, '2026-09-12 12:07:14', 0, '生产');
INSERT INTO `sys_user` VALUES (36, '郭总', '$2a$10$RudVuQ/j6Taqurdwn8eLse6bu77l3MCl8jkqvGxfyq2DxxHd5ZnXK', '郭嘉桥', '15898405117', '', 1, 1, '2026-09-12 16:50:54', 1, '2026-09-19 10:56:17', 0, NULL);
INSERT INTO `sys_user` VALUES (37, '毛老板', '$2a$10$RudVuQ/j6Taqurdwn8eLse6bu77l3MCl8jkqvGxfyq2DxxHd5ZnXK', '毛俊峰', '15963425896', '', 1, 1, '2026-09-17 23:20:24', 1, '2026-09-19 10:56:17', 0, NULL);

-- ----------------------------
-- Table structure for sys_user_process
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_process`;
CREATE TABLE `sys_user_process`  (
  `user_id` bigint(0) NOT NULL COMMENT '系统用户ID sys_user.id',
  `process_id` bigint(0) NOT NULL COMMENT '工序ID t_process_dict.id',
  PRIMARY KEY (`user_id`, `process_id`) USING BTREE,
  INDEX `idx_process_id`(`process_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户-工序权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_process
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID sys_user.id',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID sys_role.id',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (8, 2);
INSERT INTO `sys_user_role` VALUES (3, 7);
INSERT INTO `sys_user_role` VALUES (4, 7);
INSERT INTO `sys_user_role` VALUES (5, 7);
INSERT INTO `sys_user_role` VALUES (6, 7);
INSERT INTO `sys_user_role` VALUES (10, 7);
INSERT INTO `sys_user_role` VALUES (11, 7);
INSERT INTO `sys_user_role` VALUES (12, 7);
INSERT INTO `sys_user_role` VALUES (13, 7);
INSERT INTO `sys_user_role` VALUES (14, 7);
INSERT INTO `sys_user_role` VALUES (15, 7);
INSERT INTO `sys_user_role` VALUES (16, 7);
INSERT INTO `sys_user_role` VALUES (17, 7);
INSERT INTO `sys_user_role` VALUES (18, 7);
INSERT INTO `sys_user_role` VALUES (19, 7);
INSERT INTO `sys_user_role` VALUES (20, 7);
INSERT INTO `sys_user_role` VALUES (21, 7);
INSERT INTO `sys_user_role` VALUES (22, 7);
INSERT INTO `sys_user_role` VALUES (23, 7);
INSERT INTO `sys_user_role` VALUES (24, 7);
INSERT INTO `sys_user_role` VALUES (25, 7);
INSERT INTO `sys_user_role` VALUES (26, 7);
INSERT INTO `sys_user_role` VALUES (27, 7);
INSERT INTO `sys_user_role` VALUES (28, 7);
INSERT INTO `sys_user_role` VALUES (29, 7);
INSERT INTO `sys_user_role` VALUES (30, 7);
INSERT INTO `sys_user_role` VALUES (31, 7);
INSERT INTO `sys_user_role` VALUES (32, 7);
INSERT INTO `sys_user_role` VALUES (33, 7);
INSERT INTO `sys_user_role` VALUES (34, 7);
INSERT INTO `sys_user_role` VALUES (2, 8);
INSERT INTO `sys_user_role` VALUES (20, 8);
INSERT INTO `sys_user_role` VALUES (23, 8);
INSERT INTO `sys_user_role` VALUES (7, 9);
INSERT INTO `sys_user_role` VALUES (36, 14);
INSERT INTO `sys_user_role` VALUES (37, 14);

-- ----------------------------
-- Table structure for t_customer
-- ----------------------------
DROP TABLE IF EXISTS `t_customer`;
CREATE TABLE `t_customer`  (
  `customer_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `contact` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`customer_id`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户档案' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_customer
-- ----------------------------
INSERT INTO `t_customer` VALUES (1, '北辰辰玺', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (2, '刘亚亮', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (3, '徐青', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (4, '艺佳门窗', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (5, '曹总', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (6, '阔源铝业', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (7, '长沙奥雄门窗', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (8, '李进', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (9, '振升铝材', '登录二维码', '', '', '', 1, '2024-03-30 00:00:00', 1, '2024-03-30 00:00:00', 0);
INSERT INTO `t_customer` VALUES (10, '旺篮达', '登录二维码', '', '', '', 1, '2024-03-31 00:00:00', 1, '2024-03-31 00:00:00', 0);
INSERT INTO `t_customer` VALUES (11, '中圆门窗', '登录二维码', '', '', '', 1, '2024-03-31 00:00:00', 1, '2024-03-31 00:00:00', 0);
INSERT INTO `t_customer` VALUES (12, '竖美门窗', '登录二维码', '', '', '', 1, '2024-03-31 00:00:00', 1, '2024-03-31 00:00:00', 0);
INSERT INTO `t_customer` VALUES (13, '罗普斯金', '登录二维码', '', '', '', 1, '2024-03-31 00:00:00', 1, '2024-03-31 00:00:00', 0);
INSERT INTO `t_customer` VALUES (14, '神秘人', '登录二维码', '', '', '', 1, '2024-03-31 00:00:00', 1, '2024-03-31 00:00:00', 0);
INSERT INTO `t_customer` VALUES (15, '如如', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (16, '壮晋', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (17, '彭宇', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (18, '金源玻璃', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (19, '卖门哥', '登录二维码', '123', '', '', 1, '2024-04-01 00:00:00', 1, '2026-09-18 21:35:05', 0);
INSERT INTO `t_customer` VALUES (20, '邓', '登录二维码', '18973423484', '', '', 1, '2024-04-01 00:00:00', 1, '2026-09-19 08:56:13', 0);
INSERT INTO `t_customer` VALUES (21, '邓劲松', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (22, '李桌', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (23, '怀化亿合门窗', '登录二维码', '', '', '', 1, '2024-04-01 00:00:00', 1, '2024-04-01 00:00:00', 0);
INSERT INTO `t_customer` VALUES (24, '黄敏', '登录二维码', '', '', '', 1, '2024-04-03 00:00:00', 1, '2024-04-03 00:00:00', 0);
INSERT INTO `t_customer` VALUES (25, '马科斯门窗', '登录二维码', '', '', '', 1, '2024-04-03 00:00:00', 1, '2024-04-03 00:00:00', 0);
INSERT INTO `t_customer` VALUES (26, '红洁不锈钢', '登录二维码', '19635465965', '河北', '1', 1, '2024-04-03 00:00:00', 1, '2026-09-18 16:46:21', 1);
INSERT INTO `t_customer` VALUES (27, '里帕皮', '李先生', '45678963214', '广州', '待考虑驾崩', 1, '2026-09-18 16:41:28', 1, '2026-09-18 16:41:28', 0);
INSERT INTO `t_customer` VALUES (29, 'bin', '资斌', '15698742365', '深圳', '', 1, '2026-09-18 17:09:57', 1, '2026-09-18 17:09:57', 0);

-- ----------------------------
-- Table structure for t_customer_follow
-- ----------------------------
DROP TABLE IF EXISTS `t_customer_follow`;
CREATE TABLE `t_customer_follow`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `customer_id` bigint(0) NOT NULL COMMENT '客户id（关联t_customer）',
  `follow_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跟进内容',
  `follow_time` datetime(0) NULL DEFAULT NULL COMMENT '跟进时间',
  `follow_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跟进人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_id`(`customer_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户跟进记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_customer_follow
-- ----------------------------
INSERT INTO `t_customer_follow` VALUES (2, 29, '合作仓', '2026-09-18 00:00:00', '卢总', '2026-09-18 21:57:42', '2026-09-18 21:57:42', NULL, NULL, 0);

-- ----------------------------
-- Table structure for t_material_stock
-- ----------------------------
DROP TABLE IF EXISTS `t_material_stock`;
CREATE TABLE `t_material_stock`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物料唯一编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物料名称',
  `material_type` tinyint(0) NOT NULL COMMENT '1型材 2纱网 3五金配件 4辅材',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '规格型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位：米/根/公斤/套',
  `stock_num` decimal(12, 3) NOT NULL COMMENT '当前库存数量',
  `warn_num` decimal(12, 3) NULL COMMENT '安全库存预警值',
  `shelf_id` bigint(0) NULL DEFAULT NULL COMMENT '存放货架库位ID',
  `enable` tinyint(0) NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_code`(`material_code`) USING BTREE,
  INDEX `idx_shelf`(`shelf_id`) USING BTREE,
  INDEX `idx_material_type`(`material_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '原料库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_material_stock
-- ----------------------------
INSERT INTO `t_material_stock` VALUES (1, 'M001', '铝合金框型材', 1, '6米/支 壁厚1.0', '米', 498.521, 50.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-24 13:26:00');
INSERT INTO `t_material_stock` VALUES (2, 'M002', '304不锈钢金刚网', 2, '丝径0.5mm 14目', '㎡', 199.893, 20.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-24 13:26:00');
INSERT INTO `t_material_stock` VALUES (3, 'M003', '玻璃纤维纱网', 2, '18*16目 灰色', '㎡', 299.297, 30.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 23:53:50');
INSERT INTO `t_material_stock` VALUES (4, 'M004', '聚酯纤维纱网', 2, '折叠专用', '㎡', 100.000, 30.000, NULL, 1, NULL, '2026-09-19 09:47:36', 1, '2026-09-24 13:26:00');
INSERT INTO `t_material_stock` VALUES (5, 'M005', '推拉滑轮', 3, '不锈钢双轮', '个', 799.516, 100.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 23:53:50');
INSERT INTO `t_material_stock` VALUES (6, 'M006', '执手把手', 3, '铝合金', '个', 800.000, 100.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_material_stock` VALUES (7, 'M007', '传动锁具', 3, '两点锁', '套', 299.927, 50.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 23:53:50');
INSERT INTO `t_material_stock` VALUES (8, 'M008', '密封毛条', 4, '5*6mm', '米', 998.926, 200.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 23:53:50');
INSERT INTO `t_material_stock` VALUES (9, 'M009', '螺丝配件包', 4, '自攻螺丝+角码', '套', 999.786, 200.000, NULL, 1, NULL, '2026-09-19 09:47:36', NULL, '2026-09-22 23:53:50');

-- ----------------------------
-- Table structure for t_print_template
-- ----------------------------
DROP TABLE IF EXISTS `t_print_template`;
CREATE TABLE `t_print_template`  (
  `template_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WORK_ORDER' COMMENT '模板类型：WORK_ORDER工单打印',
  `paper_size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '纸张大小：A4 / A5 / 80mm小票',
  `template_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'HTML打印模板代码',
  `is_default` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否默认模板 0否1是',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=禁用',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `idx_pt_type_status`(`template_type`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '打印自定义模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_print_template
-- ----------------------------
INSERT INTO `t_print_template` VALUES (1, '工单默认打印模板', 'WORK_ORDER', 'A4', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n<meta charset=\"UTF-8\">\n<title>生产派工单</title>\n<style>\nbody{font-size:14px;font-family:\"Microsoft YaHei\",sans-serif;padding:20px;}\n.print-title{text-align:center;font-size:20px;font-weight:bold;margin-bottom:20px;}\n.info-table{width:100%;border-collapse:collapse;margin-bottom:15px;}\n.info-table td{border:1px solid #333;padding:6px 10px;}\n.table{width:100%;border-collapse:collapse;}\n.table th,.table td{border:1px solid #333;padding:8px;text-align:center;}\n.footer-text{margin-top:30px;display:flex;justify-content:space-between;padding:0 20px;}\n@media print{\n    @page {size:A4 landscape;margin:10mm;}\n}\n</style>\n</head>\n<body>\n    <div class=\"print-title\">生产派工单</div>\n\n    <table class=\"info-table\">\n        <tr>\n            <td width=\"50%\">工单编号：${orderNo}</td>\n            <td width=\"50%\">下单时间：${createTime}</td>\n        </tr>\n        <tr>\n            <td>客户名称：${customerName}</td>\n            <td>联系电话：${customerPhone}</td>\n        </tr>\n        <tr>\n            <td>产品：${productName}</td>\n            <td>尺寸：${width} × ${height} mm</td>\n        </tr>\n        <tr>\n            <td>面积：${area} ㎡</td>\n            <td>备注：${remark}</td>\n        </tr>\n    </table>\n\n    <table class=\"table\">\n        <thead>\n            <tr>\n                <th>序号</th>\n                <th>产品名称</th>\n                <th>宽(mm)</th>\n                <th>高(mm)</th>\n                <th>面积</th>\n                <th>金额</th>\n            </tr>\n        </thead>\n        <tbody>\n            <tr>\n                <td>1</td>\n                <td>${productName}</td>\n                <td>${width}</td>\n                <td>${height}</td>\n                <td>${area}</td>\n                <td>${totalMoney}</td>\n            </tr>\n        </tbody>\n    </table>\n\n    <div class=\"footer-text\">\n        <span>生产负责人签字：___________</span>\n        <span>检验签字：${customerName}</span>\n    </div>\n</body>\n</html>\n', 0, 1, NULL, '2026-07-31 10:57:13', 1, '2026-09-25 12:55:20', 1);
INSERT INTO `t_print_template` VALUES (2, '订单模板', 'WORK_ORDER', 'A4', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n<meta charset=\"UTF-8\">\n<title>生产派工单</title>\n<style>\nbody{font-size:14px;font-family:\"Microsoft YaHei\",sans-serif;padding:20px;}\n.print-title{text-align:center;font-size:20px;font-weight:bold;margin-bottom:20px;font-weight:bold;}\n.info-table{width:100%;border-collapse:collapse;margin-bottom:15px;}\n.info-table td{border:1px solid #333;padding:6px 10px;}\n.table{width:100%;border-collapse:collapse;}\n.table th,.table td{border:1px solid #333;padding:8px;text-align:center;}\n.footer-text{margin-top:30px;display:flex;justify-content:space-between;padding:0 20px;}\n@media print{\n    @page {size:A4;margin:10mm;}\n}\n</style>\n</head>\n<body>\n    <div class=\"print-title\">生产派工单</div>\n\n    <table class=\"info-table\">\n        <tr>\n            <td width=\"50%\">工单编号：${orderNo}</td>\n            <td width=\"50%\">下单时间：${createTime}</td>\n        </tr>\n        <tr>\n            <td>客户名称：${customerName}</td>\n            <td>联系电话：${customerPhone}</td>\n        </tr>\n        <tr>\n            <td>产品：${productName}</td>\n            <td>尺寸：${width} × ${height} mm</td>\n        </tr>\n        <tr>\n            <td>面积：${area} ㎡</td>\n            <td>备注：${remark}</td>\n        </tr>\n    </table>\n\n    <table class=\"table\">\n        <thead>\n            <tr>\n                <th>序号</th>\n                <th>产品名称</th>\n                <th>宽(mm)</th>\n                <th>高(mm)</th>\n                <th>面积</th>\n                <th>金额</th>\n            </tr>\n        </thead>\n        <tbody>\n            <tr>\n                <td>1</td>\n                <td>${productName}</td>\n                <td>${width}</td>\n                <td>${height}</td>\n                <td>${area}</td>\n                <td>${totalMoney}</td>\n            </tr>\n        </tbody>\n    </table>\n\n    <div class=\"footer-text\">\n        <span>生产负责人签字：___________</span>\n        <span>检验签字：${customerName}</span>\n    </div>\n</body>\n</html>\n', 0, 0, 1, '2026-09-15 08:16:47', 1, '2026-09-24 13:26:00', 1);
INSERT INTO `t_print_template` VALUES (3, '测试', '销售订单', 'A5', '${orderNo}${customerName}${customerPhone}${createTime}${width}${height}${area}${price}${totalMoney}', 1, 1, 1, '2026-09-15 22:23:39', 1, '2026-09-15 22:42:47', 1);
INSERT INTO `t_print_template` VALUES (8, '生产下料单（车间横向A4）', 'WORK_ORDER', 'A4_LANDSCAPE', '[{\"type\":\"text\",\"x\":234,\"y\":22,\"w\":320,\"fontSize\":24,\"bold\":true,\"label\":\"固贤纱窗纱门生产单\",\"id\":1},{\"type\":\"qrcode\",\"x\":20,\"y\":15,\"w\":90,\"label\":\"工单二维码\",\"id\":2},{\"type\":\"qrcode\",\"x\":950,\"y\":15,\"w\":90,\"label\":\"客户二维码\",\"id\":3},{\"type\":\"text\",\"x\":120,\"y\":120,\"w\":200,\"fontSize\":13,\"label\":\"订单编号：\",\"field\":\"orderNo\",\"id\":4},{\"type\":\"text\",\"x\":637,\"y\":45,\"w\":105,\"fontSize\":13,\"label\":\"产品规格：\",\"field\":\"productSpec\",\"id\":5},{\"type\":\"text\",\"x\":560,\"y\":120,\"w\":220,\"fontSize\":13,\"label\":\"开单日期：\",\"field\":\"createTime\",\"id\":6},{\"type\":\"line\",\"x\":20,\"y\":150,\"w\":1020,\"id\":7},{\"type\":\"text\",\"x\":25,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"客户名称：\",\"field\":\"customerName\",\"id\":8},{\"type\":\"text\",\"x\":280,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"联系电话：\",\"field\":\"customerPhone\",\"id\":9},{\"type\":\"text\",\"x\":530,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"客户地址：\",\"field\":\"customerAddress\",\"id\":10},{\"type\":\"text\",\"x\":780,\"y\":160,\"w\":260,\"fontSize\":13,\"label\":\"工程地址：\",\"field\":\"projectAddress\",\"id\":11},{\"type\":\"table\",\"x\":20,\"y\":195,\"w\":1030,\"cols\":[\"原尺寸\",\"数量\",\"颜色\",\"固定\",\"外框宽×高\",\"内框宽×高\",\"内扇宽×高\",\"孔位\",\"横杆\",\"纱网\",\"剪网尺寸\",\"加杆\",\"把手\",\"备注\"],\"colsText\":\"原尺寸,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,剪网尺寸,加杆,把手,备注\",\"id\":12},{\"type\":\"text\",\"x\":25,\"y\":420,\"w\":200,\"fontSize\":13,\"label\":\"合计数量：\",\"field\":\"totalNum\",\"id\":13},{\"type\":\"text\",\"x\":280,\"y\":420,\"w\":200,\"fontSize\":13,\"label\":\"总面积：\",\"field\":\"totalArea\",\"id\":14},{\"type\":\"text\",\"x\":560,\"y\":420,\"w\":260,\"fontSize\":13,\"label\":\"完工日期：\",\"field\":\"finishTime\",\"id\":15},{\"id\":16,\"type\":\"text\",\"x\":333,\"y\":74,\"w\":140,\"fontSize\":13,\"label\":\"工单编号：\",\"field\":\"workNo\",\"bold\":false}]', 0, 1, 1, '2026-09-24 13:38:51', 1, '2026-09-25 12:55:19', 1);
INSERT INTO `t_print_template` VALUES (9, '测试', 'WORK_ORDER', 'A4‑LANDSCAPE', '<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf‑8\">\n<style>\nbody{font‑family:\"Microsoft YaHei\",sans‑-serif;margin:10px;}\nh2{text‑align:center;margin‑bottom:12px;}\n.header‑meta{margin‑bottom:10px;font‑size:13px;}\ntable{width:100%;border‑collapse:collapse;margin‑bottom:14px;}\nth,td{border:1px solid #666;padding:6px 8px;font‑size:12px;}\n.footer‑area{margin‑top:20px;font‑size:12px;color:#555;}\n@page { size:A4 landscape; margin:10mm; }\n@media print{body{margin:0;}}\n</style>\n</head>\n<body>\n  <h2>测试单</h2>\n  <div class=\"header‑meta\"> ${orderNo} ${customerName} ${customerPhone} ${createTime}</div>\n  <table>\n    <thead><tr><th>${width}</th><th>${height}</th><th>${area}</th><th>${price}</th><th>${totalMoney}</th><th>${netMaterial}</th><th>${color}</th></tr></thead>\n    <tbody><tr><td>${width}</td><td>${height}</td><td>${area}</td><td>${price}</td><td>${totalMoney}</td><td>${netMaterial}</td><td>${color}</td></tr></tbody>\n  </table>\n  <div class=\"footer‑area\"> ${qrCode} ${createUser}</div>\n</body>\n</html>', 0, 1, 1, '2026-09-25 12:21:11', 1, '2026-09-25 12:51:06', 1);
INSERT INTO `t_print_template` VALUES (10, '生产下料单', 'WORK_ORDER', 'A4_LANDSCAPE', '[{\"type\":\"text\",\"x\":300,\"y\":18,\"w\":320,\"fontSize\":24,\"bold\":true,\"label\":\"固贤纱窗纱门生产单\",\"id\":1},{\"type\":\"qrcode\",\"x\":20,\"y\":15,\"w\":90,\"label\":\"工单二维码\",\"id\":2},{\"type\":\"qrcode\",\"x\":950,\"y\":15,\"w\":90,\"label\":\"客户二维码\",\"id\":3},{\"type\":\"text\",\"x\":120,\"y\":120,\"w\":200,\"fontSize\":13,\"label\":\"订单编号：\",\"field\":\"orderNo\",\"id\":4},{\"type\":\"text\",\"x\":340,\"y\":120,\"w\":200,\"fontSize\":13,\"label\":\"产品规格：\",\"field\":\"productSpec\",\"id\":5},{\"type\":\"text\",\"x\":560,\"y\":120,\"w\":220,\"fontSize\":13,\"label\":\"开单日期：\",\"field\":\"createTime\",\"id\":6},{\"type\":\"line\",\"x\":20,\"y\":150,\"w\":1020,\"id\":7},{\"type\":\"text\",\"x\":25,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"客户名称：\",\"field\":\"customerName\",\"id\":8},{\"type\":\"text\",\"x\":280,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"联系电话：\",\"field\":\"customerPhone\",\"id\":9},{\"type\":\"text\",\"x\":530,\"y\":160,\"w\":220,\"fontSize\":13,\"label\":\"客户地址：\",\"field\":\"customerAddress\",\"id\":10},{\"type\":\"text\",\"x\":780,\"y\":160,\"w\":260,\"fontSize\":13,\"label\":\"工程地址：\",\"field\":\"projectAddress\",\"id\":11},{\"type\":\"table\",\"x\":20,\"y\":195,\"w\":1030,\"cols\":[\"原尺寸\",\"数量\",\"颜色\",\"固定\",\"外框宽×高\",\"内框宽×高\",\"内扇宽×高\",\"孔位\",\"横杆\",\"纱网\",\"剪网尺寸\",\"加杆\",\"把手\",\"备注\"],\"colsText\":\"原尺寸,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,剪网尺寸,加杆,把手,备注\",\"id\":12},{\"type\":\"text\",\"x\":25,\"y\":420,\"w\":200,\"fontSize\":13,\"label\":\"合计数量：\",\"field\":\"totalNum\",\"id\":13},{\"type\":\"text\",\"x\":280,\"y\":420,\"w\":200,\"fontSize\":13,\"label\":\"总面积：\",\"field\":\"totalArea\",\"id\":14},{\"type\":\"text\",\"x\":560,\"y\":420,\"w\":260,\"fontSize\":13,\"label\":\"完工日期：\",\"field\":\"finishTime\",\"id\":15}]', 0, 1, 1, '2026-09-25 12:52:16', 1, '2026-09-25 13:46:05', 1);
INSERT INTO `t_print_template` VALUES (11, '生产下料单', 'WORK_ORDER', 'A4_LANDSCAPE', '[{\"id\":1,\"type\":\"qrcode\",\"x\":15,\"y\":10,\"w\":76,\"label\":\"设备扫码\"},{\"id\":2,\"type\":\"text\",\"x\":15,\"y\":90,\"w\":100,\"fontSize\":10,\"label\":\"打印二维码\"},{\"id\":3,\"type\":\"text\",\"x\":230,\"y\":14,\"w\":620,\"fontSize\":19,\"bold\":true,\"label\":\"固贤纱窗纱门生产单\",\"field\":\"specTitle\"},{\"id\":4,\"type\":\"qrcode\",\"x\":992,\"y\":10,\"w\":76,\"label\":\"客户二维码\"},{\"id\":5,\"type\":\"text\",\"x\":110,\"y\":104,\"w\":260,\"fontSize\":12,\"label\":\"客户名称：\",\"field\":\"customerName\"},{\"id\":6,\"type\":\"text\",\"x\":110,\"y\":126,\"w\":260,\"fontSize\":12,\"label\":\"客户地址：\",\"field\":\"customerAddress\"},{\"id\":7,\"type\":\"text\",\"x\":420,\"y\":104,\"w\":280,\"fontSize\":12,\"label\":\"订单编号：\",\"field\":\"orderNo\"},{\"id\":8,\"type\":\"text\",\"x\":420,\"y\":126,\"w\":280,\"fontSize\":12,\"label\":\"工程地址：\",\"field\":\"projectAddress\"},{\"id\":9,\"type\":\"text\",\"x\":720,\"y\":104,\"w\":300,\"fontSize\":12,\"label\":\"交货日期：\",\"field\":\"deliverDate\"},{\"id\":10,\"type\":\"text\",\"x\":720,\"y\":126,\"w\":300,\"fontSize\":12,\"label\":\"产品名称：\",\"field\":\"productName\"},{\"id\":11,\"type\":\"line\",\"x\":15,\"y\":150,\"w\":1050},{\"id\":12,\"type\":\"table\",\"x\":15,\"y\":162,\"w\":1050,\"cols\":[\"总宽×总高\",\"数量\",\"颜色\",\"固定\",\"外框宽×高\",\"内框宽×高\",\"内扇宽×高\",\"孔位\",\"横杆\",\"纱网\",\"上下纱宽×高\",\"中纱宽×高\",\"加杆\",\"把手\",\"备注\"],\"colsText\":\"总宽×总高,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,上下纱宽×高,中纱宽×高,加杆,把手,备注\"}]', 1, 1, 1, '2026-09-25 13:26:56', 1, '2026-09-25 13:56:22', 0);
INSERT INTO `t_print_template` VALUES (14, '生产下料单', 'WORK_ORDER', 'A4_LANDSCAPE', '[{\"id\":1,\"type\":\"qrcode\",\"x\":15,\"y\":10,\"w\":76,\"label\":\"设备扫码\"},{\"id\":2,\"type\":\"text\",\"x\":15,\"y\":90,\"w\":100,\"fontSize\":10,\"label\":\"打印二维码\"},{\"id\":3,\"type\":\"text\",\"x\":230,\"y\":14,\"w\":620,\"fontSize\":19,\"bold\":true,\"label\":\"固贤纱窗纱门生产单\",\"field\":\"specTitle\"},{\"id\":4,\"type\":\"qrcode\",\"x\":992,\"y\":10,\"w\":76,\"label\":\"客户二维码\"},{\"id\":5,\"type\":\"text\",\"x\":110,\"y\":104,\"w\":260,\"fontSize\":12,\"label\":\"客户名称：\",\"field\":\"customerName\"},{\"id\":6,\"type\":\"text\",\"x\":110,\"y\":126,\"w\":260,\"fontSize\":12,\"label\":\"客户地址：\",\"field\":\"customerAddress\"},{\"id\":7,\"type\":\"text\",\"x\":420,\"y\":104,\"w\":280,\"fontSize\":12,\"label\":\"订单编号：\",\"field\":\"orderNo\"},{\"id\":8,\"type\":\"text\",\"x\":420,\"y\":126,\"w\":280,\"fontSize\":12,\"label\":\"工程地址：\",\"field\":\"projectAddress\"},{\"id\":9,\"type\":\"text\",\"x\":720,\"y\":104,\"w\":300,\"fontSize\":12,\"label\":\"交货日期：\",\"field\":\"deliverDate\"},{\"id\":10,\"type\":\"text\",\"x\":720,\"y\":126,\"w\":300,\"fontSize\":12,\"label\":\"产品名称：\",\"field\":\"productName\"},{\"id\":11,\"type\":\"line\",\"x\":15,\"y\":150,\"w\":1050},{\"id\":12,\"type\":\"table\",\"x\":15,\"y\":162,\"w\":1050,\"cols\":[\"总宽×总高\",\"数量\",\"颜色\",\"固定\",\"外框宽×高\",\"内框宽×高\",\"内扇宽×高\",\"孔位\",\"横杆\",\"纱网\",\"上下纱宽×高\",\"中纱宽×高\",\"加杆\",\"把手\",\"备注\"],\"colsText\":\"总宽×总高,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,上下纱宽×高,中纱宽×高,加杆,把手,备注\"},{\"id\":13,\"type\":\"text\",\"x\":40,\"y\":364,\"w\":140,\"fontSize\":13,\"label\":\"工单编号：\",\"field\":\"workNo\",\"bold\":false},{\"id\":15,\"type\":\"text\",\"x\":40,\"y\":416,\"w\":140,\"fontSize\":13,\"label\":\"产品规格：\",\"field\":\"productSpec\",\"bold\":false},{\"id\":17,\"type\":\"text\",\"x\":40,\"y\":468,\"w\":140,\"fontSize\":13,\"label\":\"产品系列：\",\"field\":\"seriesName\",\"bold\":false}]', 0, 1, 1, '2026-09-25 13:55:36', 1, '2026-09-25 13:56:21', 0);

-- ----------------------------
-- Table structure for t_process_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_process_dict`;
CREATE TABLE `t_process_dict`  (
  `process_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '工序ID',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序编码',
  `process_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序名称',
  `process_sort` int(0) NOT NULL COMMENT '生产排序号(决定先后顺序)',
  `post_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对应工人岗位类型',
  `unit_price` decimal(10, 2) NULL COMMENT '工序计件单价(元/件)',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`process_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产工序字典(动态可扩展)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_process_dict
-- ----------------------------
INSERT INTO `t_process_dict` VALUES (1, 'CUT', '开料', 1, 'cut', 0.80, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (2, 'ASSEMBLE', '组装', 2, 'assemble', 2.50, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (3, 'CUT_NET', '切网', 3, 'cut_net', 0.80, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (4, 'SAND', '磨砂', 4, 'sand', 0.50, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (5, 'CHECK', '质检', 5, 'check', 0.50, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (6, 'PACKAGE', '打包', 6, 'package', 1.00, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);
INSERT INTO `t_process_dict` VALUES (7, 'STOCK_IN', '入库上架', 7, 'stock_in', 0.50, 1, '', '2026-08-03 09:09:46', '2026-09-22 22:53:30', 0);

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product`  (
  `product_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品编码',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
  `product_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品分类：平开纱窗/推拉纱窗/金刚网纱窗/折叠纱窗',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '㎡' COMMENT '计价单位',
  `unit_price` decimal(12, 2) NULL COMMENT '基础单价（元/㎡或元/件）',
  `price_type` tinyint(0) NULL DEFAULT 1 COMMENT '计价方式：1按面积 2按件',
  `min_area` decimal(10, 4) NULL COMMENT '最小起算方（㎡），单扇面积不足按此计',
  `default_color` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认颜色',
  `default_material` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认材质',
  `open_direction` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认开启方向',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记：0正常 1删除',
  PRIMARY KEY (`product_id`) USING BTREE,
  UNIQUE INDEX `uk_product_code`(`product_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产品档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_product
-- ----------------------------
INSERT INTO `t_product` VALUES (1, 'P001', '金刚网防盗纱窗(平开)', '金刚网纱窗', '按㎡定制', '㎡', 280.00, 1, 0.5000, '灰色', '304不锈钢金刚网', '外开', 1, '含五金配件', NULL, '2026-09-19 09:25:36', NULL, '2026-09-19 09:25:36', 0);
INSERT INTO `t_product` VALUES (2, 'P002', '普通推拉纱窗', '推拉纱窗', '按㎡定制', '㎡', 120.00, 1, 0.3000, '白色', '玻璃纤维网', '左右推拉', 1, '常规款', NULL, '2026-09-19 09:25:36', NULL, '2026-09-19 09:25:36', 0);
INSERT INTO `t_product` VALUES (3, 'P003', '折叠隐形纱窗', '折叠纱窗', '按㎡定制', '㎡', 160.00, 1, 0.3000, '灰色', '聚酯纤维网', '折叠', 1, '折叠收纳', NULL, '2026-09-19 09:25:36', NULL, '2026-09-19 09:25:36', 0);
INSERT INTO `t_product` VALUES (5, 'P006', '黄金金刚纱窗', '金刚网纱窗', '', '㎡', 10000.00, 1, 90000.0000, '', '', '', 1, '', 1, '2026-09-19 11:13:48', 1, '2026-09-19 11:13:48', 0);

-- ----------------------------
-- Table structure for t_product_bom
-- ----------------------------
DROP TABLE IF EXISTS `t_product_bom`;
CREATE TABLE `t_product_bom`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint(0) NULL DEFAULT NULL COMMENT '纱窗产品ID(字典产品时为空)',
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品字典类型 style_xxx',
  `material_id` bigint(0) NOT NULL COMMENT '原料物料ID t_material_stock.id',
  `use_num` decimal(10, 3) NOT NULL COMMENT '单套产品物料耗用数量',
  `loss_rate` decimal(5, 2) NOT NULL DEFAULT 0.05 COMMENT '物料损耗率 0.05=5%',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记：0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product`(`product_id`) USING BTREE,
  INDEX `idx_material`(`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '纱窗产品BOM物料清单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_product_bom
-- ----------------------------
INSERT INTO `t_product_bom` VALUES (11, NULL, 'style_8k_sanjie', 1, 2.000, 0.05, 1, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (12, NULL, 'style_8k_sanjie', 3, 1.100, 0.08, 2, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (13, NULL, 'style_8k_sanjie', 5, 0.800, 0.02, 3, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (14, NULL, 'style_8k_sanjie', 8, 1.500, 0.05, 4, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (15, NULL, 'style_8k_sanjie', 9, 0.300, 0.00, 5, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (16, NULL, 'style_kuangzhongkuang', 1, 2.500, 0.05, 1, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (17, NULL, 'style_kuangzhongkuang', 2, 1.100, 0.08, 2, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (18, NULL, 'style_kuangzhongkuang', 7, 0.800, 0.02, 3, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (19, NULL, 'style_kuangzhongkuang', 8, 1.500, 0.05, 4, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (20, NULL, 'style_kuangzhongkuang', 9, 0.400, 0.00, 5, NULL, '2026-09-22 22:54:01', NULL, '2026-09-22 22:54:01', 0);
INSERT INTO `t_product_bom` VALUES (21, 5, NULL, 1, 1.000, 0.05, 1, 1, '2026-09-26 14:58:45', 1, '2026-09-26 14:58:45', 0);

-- ----------------------------
-- Table structure for t_product_instock
-- ----------------------------
DROP TABLE IF EXISTS `t_product_instock`;
CREATE TABLE `t_product_instock`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint(0) NOT NULL COMMENT '关联工单ID t_work_order.id',
  `order_id` bigint(0) NOT NULL COMMENT '关联销售订单ID t_sales_order.id',
  `shelf_id` bigint(0) NOT NULL COMMENT '入库货架库位ID t_shelf.id',
  `in_num` int(0) NOT NULL DEFAULT 0 COMMENT '入库成品总数量',
  `instock_status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '1待入库 2已入库完成',
  `stock_user_id` bigint(0) NULL DEFAULT NULL COMMENT '操作仓管员ID',
  `instock_time` datetime(0) NULL DEFAULT NULL COMMENT '实际入库时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '入库备注',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_order`(`work_order_id`) USING BTREE,
  INDEX `idx_shelf`(`shelf_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成品入库单据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_product_instock
-- ----------------------------
INSERT INTO `t_product_instock` VALUES (1, 2, 7, 2, 6, 2, 1, '2026-09-22 23:11:44', '闭环验证', 1, '2026-09-22 23:11:44', 1, '2026-09-22 23:11:44');
INSERT INTO `t_product_instock` VALUES (2, 3, 8, 1, 2, 2, 1, '2026-09-22 23:53:52', '订单8闭环', 1, '2026-09-22 23:53:52', 1, '2026-09-22 23:53:52');

-- ----------------------------
-- Table structure for t_sales_order
-- ----------------------------
DROP TABLE IF EXISTS `t_sales_order`;
CREATE TABLE `t_sales_order`  (
  `order_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '客户订单主键',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `order_status` tinyint(0) NULL DEFAULT 0 COMMENT '订单状态：0待审核 1已审核(待排产) 2生产中 3已完工 4已发货 5已完成 6已驳回 7已取消',
  `order_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '正常单' COMMENT '订单类型：正常单/加急单/经销商单/样品单',
  `customer_id` bigint(0) NOT NULL COMMENT '关联客户ID',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称（冗余，便于列表展示）',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装/收货地址',
  `order_date` date NULL DEFAULT NULL COMMENT '下单日期',
  `expect_date` date NULL DEFAULT NULL COMMENT '期望交期',
  `total_area` decimal(12, 4) NOT NULL COMMENT '订单总纱窗面积',
  `projection_area` decimal(12, 4) NOT NULL COMMENT '投影面积(㎡)',
  `big_board_num` int(0) NOT NULL DEFAULT 0 COMMENT '大板数',
  `product_amount` decimal(14, 2) NULL COMMENT '产品金额合计（明细行金额之和）',
  `craft_fee` decimal(12, 2) NULL COMMENT '特殊工艺加价',
  `urgent_fee` decimal(12, 2) NULL COMMENT '加急费',
  `freight` decimal(12, 2) NULL COMMENT '运费',
  `discount_amount` decimal(12, 2) NULL COMMENT '优惠/减价',
  `total_amount` decimal(14, 2) NULL COMMENT '订单总金额',
  `finance_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '财务状态：0未收账 1部分已收账 2已收账 3已结清',
  `paid_amount` decimal(14, 2) NOT NULL COMMENT '已付金额',
  `receive_amount` decimal(14, 2) NOT NULL COMMENT '实收金额(累计)',
  `unpaid_amount` decimal(14, 2) NOT NULL COMMENT '未付金额',
  `profit_amount` decimal(14, 2) NOT NULL COMMENT '利润',
  `gross_profit_rate` decimal(8, 4) NOT NULL COMMENT '毛利率',
  `sub_order_count` int(0) NOT NULL DEFAULT 0 COMMENT '子单数量',
  `receive_time` datetime(0) NULL DEFAULT NULL COMMENT '最近收款时间',
  `audit_by` bigint(0) NULL DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `delivery_time` datetime(0) NULL DEFAULT NULL COMMENT '发货时间',
  `finish_time` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '订单备注',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '套' COMMENT '单位',
  `install_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '客户安装' COMMENT '安装方式',
  `designer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设计师',
  `splitter` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拆单师',
  `salesman` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务员',
  `logistics` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流',
  `terminal_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '终端地址',
  `customer_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户来源',
  `create_by` bigint(0) NOT NULL COMMENT '创建人（文员）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_customer`(`customer_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户尺寸订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_sales_order
-- ----------------------------
INSERT INTO `t_sales_order` VALUES (6, 'SO20260919001', 5, '正常单', 1, '北辰辰玺', '张经理', '13800001111', NULL, '2026-09-19', '2026-09-30', 3.0540, 12.5000, 3, 583.27, 100.00, 0.00, 80.00, 50.00, 713.27, 2, 713.27, 713.27, 0.00, 180.00, 0.2524, 2, '2026-09-26 17:48:32', 2, '2026-09-19 10:38:12', NULL, '2026-09-19 10:38:19', '2026-09-19 10:38:19', NULL, '订单管理模块联调测试单，请勿删除', '固贤', '套', '客户安装', '李设计', '王拆单', '陈业务', '顺丰物流', '长沙市雨花区北辰三角洲 3 栋 1802', '老客户转介绍', 2, '2026-09-19 10:38:12', 1, '2026-09-26 17:48:32', 0);
INSERT INTO `t_sales_order` VALUES (7, 'SO20260919002', 5, '正常单', 22, '李桌', '登录二维码', '15898405119', NULL, '2026-09-19', '2026-09-26', 0.4320, 10.0000, 12, 10.20, 0.00, 0.00, 0.00, 0.00, 10.20, 2, 10.20, 10.20, 0.00, 0.00, 0.0000, 1, '2026-09-26 17:49:43', 2, '2026-09-19 11:18:03', NULL, '2026-09-22 23:41:40', '2026-09-24 13:48:24', NULL, '测试用', '框中款', '套', '自行安装', '卢工', '刘工', '卢工', '顺丰', '长沙', '抖音', 1, '2026-09-19 11:10:07', 1, '2026-09-26 17:49:43', 0);
INSERT INTO `t_sales_order` VALUES (8, 'SO20260922001', 5, '加急单', 21, '邓劲松', '登录二维码', '17378089633', NULL, '2026-09-22', '2026-09-30', 0.2500, 0.0000, 0, 53.76, 0.00, 0.00, 0.00, 0.00, 53.76, 2, 53.76, 53.76, 0.00, 0.00, 0.0000, 2, '2026-09-22 23:55:55', 1, '2026-09-22 23:39:26', NULL, '2026-09-22 23:53:52', '2026-09-22 23:53:52', NULL, '加急加急', '框中框', '套', '自行安装', '卢工', '卢工', '卢工', '顺丰', '杭州余杭区仓益绿苑', '抖音', 1, '2026-09-22 23:38:06', 1, '2026-09-22 23:55:55', 0);
INSERT INTO `t_sales_order` VALUES (9, 'SO20260922002', 2, '正常单', 25, '马科斯门窗', '登录二维码', '15986963265', NULL, '2026-09-22', '2026-09-30', 4800.0000, 0.0000, 0, 460800.00, 0.00, 0.00, 0.00, 0.00, 460800.00, 2, 460800.00, 460800.00, 0.00, 0.00, 0.0000, 1, '2026-09-26 17:49:47', 1, '2026-09-22 23:58:32', NULL, NULL, NULL, NULL, '', '', '套', '客户安装', '', '', '', '京东', '杭州', '', 1, '2026-09-22 23:58:12', 1, '2026-09-26 17:49:47', 0);
INSERT INTO `t_sales_order` VALUES (10, 'SO20260924001', 1, '正常单', 17, '彭宇', '登录二维码', '1', NULL, '2026-09-24', '2026-09-25', 0.0000, 0.0000, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 0.00, 0.00, 0.00, 0.00, 0.0000, 1, NULL, 1, '2026-09-24 14:02:08', NULL, NULL, NULL, NULL, '1', '1', '套', '客户安装', '1', '1', '1', '', '1', '', 1, '2026-09-24 14:01:34', 1, '2026-09-24 14:02:08', 0);

-- ----------------------------
-- Table structure for t_sales_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_sales_order_item`;
CREATE TABLE `t_sales_order_item`  (
  `item_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint(0) NOT NULL COMMENT '归属订单ID',
  `sub_order_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子单号',
  `product_id` bigint(0) NULL DEFAULT NULL COMMENT '产品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品名称（快照）',
  `product_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `item_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品目(字典系列名称)',
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品字典类型 style_xxx',
  `color` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '颜色',
  `net_material` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网子',
  `handle` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '把手',
  `lock_set` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '锁具',
  `handle_direction` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '把手方向',
  `add_rod` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加杆',
  `fixed_bottom` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下固定',
  `square_board` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方板规格(5号方板等)',
  `material` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '材质',
  `open_direction` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开启方向',
  `unit_price` decimal(12, 2) NULL COMMENT '单价（元/㎡，快照）',
  `width` decimal(10, 2) NOT NULL COMMENT '宽度(米)',
  `height` decimal(10, 2) NOT NULL COMMENT '高度(米)',
  `deduct_width` decimal(10, 2) NOT NULL COMMENT '扣宽(mm)',
  `net_width` decimal(10, 2) NULL DEFAULT NULL COMMENT '扣宽后净宽(mm)，参与面积计算',
  `single_area` decimal(12, 4) NOT NULL COMMENT '单扇面积 宽*高',
  `min_area` decimal(10, 4) NULL COMMENT '最小起算方（㎡，快照）',
  `calc_type` tinyint(0) NOT NULL DEFAULT 1 COMMENT '计算方式：1按面积 2按件',
  `charge_area` decimal(12, 4) NULL COMMENT '单扇计费面积=max(单扇面积,最小起算方)',
  `num` int(0) NOT NULL DEFAULT 1 COMMENT '数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '套' COMMENT '单位',
  `item_total_area` decimal(12, 4) NOT NULL COMMENT '该款总面积 single_area * num',
  `line_amount` decimal(14, 2) NULL COMMENT '行金额=行总面积×单价',
  `item_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '行状态：0订单未受理 1订单已受理 2生产中 3已完工 4已发货 5已完成 9已取消',
  `sale_price_type` tinyint(0) NOT NULL DEFAULT 1 COMMENT '销售计价方式：1按面积 2按件 3按公式',
  `freight` decimal(12, 2) NOT NULL COMMENT '行运费',
  `receive_amount` decimal(14, 2) NOT NULL COMMENT '行实收金额',
  `profit_amount` decimal(14, 2) NOT NULL COMMENT '行利润',
  `sales_owner` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售商/下单员',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前工序编码(生产预留)',
  `process_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前工序名称(生产预留)',
  `flow_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产流程(生产预留)',
  `produce_progress` int(0) NOT NULL DEFAULT 0 COMMENT '生产进度%(生产预留)',
  `work_order_id` bigint(0) NULL DEFAULT NULL COMMENT '关联生产工单ID(生产预留)',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order`(`order_id`) USING BTREE,
  INDEX `idx_sub_order_no`(`sub_order_no`) USING BTREE,
  INDEX `idx_item_status`(`item_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '纱窗尺寸明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_sales_order_item
-- ----------------------------
INSERT INTO `t_sales_order_item` VALUES (8, 6, 'SO20260919001-01', NULL, '8K大料三节', '8K三节系列', '8K三节系列', 'style_8k_sanjie', '金刚灰', '8K金刚网', '锌合金把手', '月牙锁', '左', '加一根', '下固定50', '5号方板', NULL, '外开', 168.00, 1210.00, 710.00, 10.00, 1200.00, 0.8520, 0.0000, 1, 0.8520, 2, '套', 1.7040, 286.27, 5, 1, 0.00, 0.00, 120.00, '文员小张', NULL, NULL, NULL, 100, NULL, '客厅用');
INSERT INTO `t_sales_order_item` VALUES (9, 6, 'SO20260919001-02', NULL, '金刚网框中框', '框中框系列', '框中框系列', 'style_kuangzhongkuang', '香槟金', '不锈钢网', '塑料把手', '按锁', '右', '不加杆', '无', NULL, NULL, '内开', 220.00, 900.00, 1500.00, 0.00, 900.00, 1.3500, 0.0000, 1, 1.3500, 1, '套', 1.3500, 297.00, 5, 1, 0.00, 0.00, 60.00, '文员小张', NULL, NULL, NULL, 100, NULL, '卧室用');
INSERT INTO `t_sales_order_item` VALUES (10, 7, 'SO20260919002-01', NULL, '8K大料三节', '8K三节系列', '8K三节系列', 'style_8k_sanjie', '无', '无', '无', '无', '无', '无', '无', '无', '无', '无', 23.60, 200.00, 400.00, 20.00, 180.00, 0.0720, 0.0000, 1, 0.0720, 6, '套', 0.4320, 10.20, 5, 1, 0.00, 0.00, 0.00, '', NULL, NULL, NULL, 0, 2, '');
INSERT INTO `t_sales_order_item` VALUES (13, 8, 'SO20260922001-01', NULL, '金刚网框中框', '框中框系列', '框中框系列', 'style_kuangzhongkuang', '红', '纱网', '铁把手', '卡扣', '右', '不加', '无', '无', '金刚', '无', 160.00, 200.00, 600.00, 50.00, 150.00, 0.0900, 0.0000, 1, 0.0900, 1, '套', 0.0900, 14.40, 5, 1, 0.00, 0.00, 0.00, '', NULL, NULL, NULL, 100, 3, '');
INSERT INTO `t_sales_order_item` VALUES (14, 8, 'SO20260922001-02', NULL, '8K大料三节', '8K三节系列', '8K三节系列', 'style_8k_sanjie', '绿', '纱网', '无', '无', '无', '不加', '无', '无', '无', '无', 246.00, 300.00, 800.00, 100.00, 200.00, 0.1600, 0.0000, 1, 0.1600, 1, '套', 0.1600, 39.36, 5, 1, 0.00, 0.00, 0.00, '', NULL, NULL, NULL, 100, 3, '');
INSERT INTO `t_sales_order_item` VALUES (15, 9, 'SO20260922002-01', NULL, '8K极窄四节-4节纱', '8K三节系列', '8K三节系列', 'style_8k_sanjie', '无', '无', '无', '无', '无', '无', '无', '无', '无', '无', 96.00, 60000.00, 80000.00, 0.00, 60000.00, 4800.0000, 0.0000, 1, 4800.0000, 1, '套', 4800.0000, 460800.00, 2, 1, 0.00, 0.00, 0.00, '', NULL, NULL, NULL, 0, 4, '');
INSERT INTO `t_sales_order_item` VALUES (16, 10, 'SO20260924001-01', NULL, '8K极窄三节-全防护', '8K三节系列', '8K三节系列', 'style_8k_sanjie', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', 10000.00, 1.00, 1.00, 0.00, 1.00, 0.0000, 0.0000, 1, 0.0000, 1, '套', 0.0000, 0.00, 1, 1, 0.00, 0.00, 0.00, '', NULL, NULL, NULL, 0, NULL, '');

-- ----------------------------
-- Table structure for t_shelf
-- ----------------------------
DROP TABLE IF EXISTS `t_shelf`;
CREATE TABLE `t_shelf`  (
  `shelf_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '货架ID',
  `shelf_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货架编号 01/02/03/04',
  `shelf_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货架名称',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `sort` int(0) NULL DEFAULT 0,
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`shelf_id`) USING BTREE,
  UNIQUE INDEX `uk_shelf_code`(`shelf_code`) USING BTREE,
  INDEX `idx_del`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '货架表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_shelf
-- ----------------------------
INSERT INTO `t_shelf` VALUES (1, '01', '01成品货架', 1, 1, NULL, '2026-07-31 10:57:13', NULL, '2026-07-31 10:57:13', 0);
INSERT INTO `t_shelf` VALUES (2, '02', '02成品货架', 1, 2, NULL, '2026-07-31 10:57:13', NULL, '2026-07-31 10:57:13', 0);
INSERT INTO `t_shelf` VALUES (3, '03', '03成品货架', 1, 3, NULL, '2026-07-31 10:57:13', NULL, '2026-07-31 10:57:13', 0);
INSERT INTO `t_shelf` VALUES (4, '04', '04成品货架', 1, 4, NULL, '2026-07-31 10:57:13', NULL, '2026-07-31 10:57:13', 0);

-- ----------------------------
-- Table structure for t_stock_check
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_check`;
CREATE TABLE `t_stock_check`  (
  `check_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `check_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单号',
  `check_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0草稿 1已过账',
  `total_diff` decimal(12, 3) NOT NULL COMMENT '差异汇总',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`check_id`) USING BTREE,
  UNIQUE INDEX `uk_check_no`(`check_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存盘点单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_check
-- ----------------------------

-- ----------------------------
-- Table structure for t_stock_check_item
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_check_item`;
CREATE TABLE `t_stock_check_item`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `check_id` bigint(0) NOT NULL COMMENT '盘点单ID',
  `material_id` bigint(0) NOT NULL COMMENT '物料ID',
  `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `book_num` decimal(12, 3) NOT NULL COMMENT '账面数',
  `real_num` decimal(12, 3) NULL DEFAULT NULL COMMENT '实盘数',
  `diff_num` decimal(12, 3) NULL COMMENT '差异=实盘-账面',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_check_id`(`check_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '盘点单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_check_item
-- ----------------------------

-- ----------------------------
-- Table structure for t_stock_record
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_record`;
CREATE TABLE `t_stock_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `material_id` bigint(0) NOT NULL COMMENT '物料ID',
  `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `biz_type` tinyint(0) NOT NULL COMMENT '1期初入库 2采购入库 3生产领料 4盘点调整 5退货出库',
  `in_num` decimal(12, 3) NOT NULL COMMENT '入库数量',
  `out_num` decimal(12, 3) NOT NULL COMMENT '出库数量',
  `after_num` decimal(12, 3) NOT NULL COMMENT '变动后库存',
  `relate_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '关联单类型 WORK_ORDER/PURCHASE',
  `relate_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '关联单号(工单号等)',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_material`(`material_id`) USING BTREE,
  INDEX `idx_relate`(`relate_type`, `relate_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_record
-- ----------------------------
INSERT INTO `t_stock_record` VALUES (1, 1, 'M001', '铝合金框型材', 1, 500.000, 0.000, 500.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (2, 2, 'M002', '304不锈钢金刚网', 1, 200.000, 0.000, 200.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (3, 3, 'M003', '玻璃纤维纱网', 1, 300.000, 0.000, 300.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (4, 4, 'M004', '聚酯纤维纱网', 1, 100.000, 0.000, 100.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (5, 5, 'M005', '推拉滑轮', 1, 800.000, 0.000, 800.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (6, 6, 'M006', '执手把手', 1, 800.000, 0.000, 800.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (7, 7, 'M007', '传动锁具', 1, 300.000, 0.000, 300.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (8, 8, 'M008', '密封毛条', 1, 1000.000, 0.000, 1000.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (9, 9, 'M009', '螺丝配件包', 1, 1000.000, 0.000, 1000.000, '', '', '期初库存录入', NULL, '2026-09-22 22:54:02');
INSERT INTO `t_stock_record` VALUES (16, 1, 'M001', '铝合金框型材', 3, 0.000, 0.907, 499.093, 'WORK_ORDER', 'WO20260922001', '生产领料-WO20260922001', 1, '2026-09-22 23:11:43');
INSERT INTO `t_stock_record` VALUES (17, 3, 'M003', '玻璃纤维纱网', 3, 0.000, 0.513, 299.487, 'WORK_ORDER', 'WO20260922001', '生产领料-WO20260922001', 1, '2026-09-22 23:11:43');
INSERT INTO `t_stock_record` VALUES (18, 5, 'M005', '推拉滑轮', 3, 0.000, 0.353, 799.647, 'WORK_ORDER', 'WO20260922001', '生产领料-WO20260922001', 1, '2026-09-22 23:11:43');
INSERT INTO `t_stock_record` VALUES (19, 8, 'M008', '密封毛条', 3, 0.000, 0.680, 999.320, 'WORK_ORDER', 'WO20260922001', '生产领料-WO20260922001', 1, '2026-09-22 23:11:43');
INSERT INTO `t_stock_record` VALUES (20, 9, 'M009', '螺丝配件包', 3, 0.000, 0.130, 999.870, 'WORK_ORDER', 'WO20260922001', '生产领料-WO20260922001', 1, '2026-09-22 23:11:43');
INSERT INTO `t_stock_record` VALUES (26, 1, 'M001', '铝合金框型材', 3, 0.000, 0.572, 508.521, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (27, 2, 'M002', '304不锈钢金刚网', 3, 0.000, 0.107, 194.893, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (28, 7, 'M007', '传动锁具', 3, 0.000, 0.073, 299.927, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (29, 8, 'M008', '密封毛条', 3, 0.000, 0.394, 998.926, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (30, 9, 'M009', '螺丝配件包', 3, 0.000, 0.084, 999.786, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (31, 3, 'M003', '玻璃纤维纱网', 3, 0.000, 0.190, 299.297, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');
INSERT INTO `t_stock_record` VALUES (32, 5, 'M005', '推拉滑轮', 3, 0.000, 0.131, 799.516, 'WORK_ORDER', 'WO20260922002', '生产领料-WO20260922002', 1, '2026-09-22 23:53:50');

-- ----------------------------
-- Table structure for t_work_order
-- ----------------------------
DROP TABLE IF EXISTS `t_work_order`;
CREATE TABLE `t_work_order`  (
  `work_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `work_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单号(二维码内容)',
  `sales_order_id` bigint(0) NOT NULL COMMENT '销售订单ID',
  `customer_id` bigint(0) NOT NULL COMMENT '客户ID',
  `shelf_id` bigint(0) NOT NULL COMMENT '预分配入库货架ID',
  `total_area` decimal(10, 2) NULL COMMENT '工单总面积',
  `work_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAIT_PROCESS' COMMENT '工单状态',
  `is_rework` tinyint(0) NULL DEFAULT 0 COMMENT '是否返工 0否1是',
  `finish_time` datetime(0) NULL DEFAULT NULL COMMENT '完工时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `del_flag` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`work_id`) USING BTREE,
  UNIQUE INDEX `work_no`(`work_no`) USING BTREE,
  INDEX `idx_work_no`(`work_no`) USING BTREE,
  INDEX `idx_sales_order`(`sales_order_id`) USING BTREE,
  INDEX `idx_shelf`(`shelf_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产工单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_work_order
-- ----------------------------
INSERT INTO `t_work_order` VALUES (2, 'WO20260922001', 7, 22, 1, 0.43, 'FINISHED', 0, '2026-09-22 23:11:44', '', 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:44', 0);
INSERT INTO `t_work_order` VALUES (3, 'WO20260922002', 8, 21, 1, 0.25, 'FINISHED', 0, '2026-09-22 23:53:52', '', 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:52', 0);
INSERT INTO `t_work_order` VALUES (4, 'WO20260922003', 9, 25, 1, 4800.00, 'PROCESSING', 0, NULL, '', 1, '2026-09-22 23:59:04', 1, '2026-09-24 13:58:08', 0);

-- ----------------------------
-- Table structure for t_work_order_material
-- ----------------------------
DROP TABLE IF EXISTS `t_work_order_material`;
CREATE TABLE `t_work_order_material`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `work_id` bigint(0) NOT NULL COMMENT '工单ID',
  `material_id` bigint(0) NOT NULL COMMENT '物料ID',
  `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '物料编码快照',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '物料名称快照',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '单位',
  `require_num` decimal(12, 3) NOT NULL COMMENT '需求数量(含损耗)',
  `picked_num` decimal(12, 3) NOT NULL COMMENT '已领数量',
  `calc_type` tinyint(0) NOT NULL DEFAULT 1 COMMENT '1按面积㎡ 2按件',
  `loss_rate` decimal(5, 2) NULL DEFAULT 0.05,
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work`(`work_id`) USING BTREE,
  INDEX `idx_material`(`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单物料需求清单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_work_order_material
-- ----------------------------
INSERT INTO `t_work_order_material` VALUES (6, 2, 1, 'M001', '铝合金框型材', '米', 0.907, 0.907, 1, 0.05, 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_order_material` VALUES (7, 2, 3, 'M003', '玻璃纤维纱网', '㎡', 0.513, 0.513, 1, 0.08, 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_order_material` VALUES (8, 2, 5, 'M005', '推拉滑轮', '个', 0.353, 0.353, 1, 0.02, 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_order_material` VALUES (9, 2, 8, 'M008', '密封毛条', '米', 0.680, 0.680, 1, 0.05, 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_order_material` VALUES (10, 2, 9, 'M009', '螺丝配件包', '套', 0.130, 0.130, 1, 0.00, 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_order_material` VALUES (11, 3, 1, 'M001', '铝合金框型材', '米', 0.572, 0.572, 1, 0.05, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (12, 3, 2, 'M002', '304不锈钢金刚网', '㎡', 0.107, 0.107, 1, 0.08, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (13, 3, 7, 'M007', '传动锁具', '套', 0.073, 0.073, 1, 0.02, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (14, 3, 8, 'M008', '密封毛条', '米', 0.394, 0.394, 1, 0.05, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (15, 3, 9, 'M009', '螺丝配件包', '套', 0.084, 0.084, 1, 0.00, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (16, 3, 3, 'M003', '玻璃纤维纱网', '㎡', 0.190, 0.190, 1, 0.08, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (17, 3, 5, 'M005', '推拉滑轮', '个', 0.131, 0.131, 1, 0.02, 1, '2026-09-22 23:53:07', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_order_material` VALUES (18, 4, 1, 'M001', '铝合金框型材', '米', 10080.000, 0.000, 1, 0.05, 1, '2026-09-22 23:59:04', 1, '2026-09-22 23:59:04');
INSERT INTO `t_work_order_material` VALUES (19, 4, 3, 'M003', '玻璃纤维纱网', '㎡', 5702.400, 0.000, 1, 0.08, 1, '2026-09-22 23:59:04', 1, '2026-09-22 23:59:04');
INSERT INTO `t_work_order_material` VALUES (20, 4, 5, 'M005', '推拉滑轮', '个', 3916.800, 0.000, 1, 0.02, 1, '2026-09-22 23:59:04', 1, '2026-09-22 23:59:04');
INSERT INTO `t_work_order_material` VALUES (21, 4, 8, 'M008', '密封毛条', '米', 7560.000, 0.000, 1, 0.05, 1, '2026-09-22 23:59:04', 1, '2026-09-22 23:59:04');
INSERT INTO `t_work_order_material` VALUES (22, 4, 9, 'M009', '螺丝配件包', '套', 1440.000, 0.000, 1, 0.00, 1, '2026-09-22 23:59:04', 1, '2026-09-22 23:59:04');

-- ----------------------------
-- Table structure for t_work_process_record
-- ----------------------------
DROP TABLE IF EXISTS `t_work_process_record`;
CREATE TABLE `t_work_process_record`  (
  `record_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `work_id` bigint(0) NOT NULL COMMENT '工单ID',
  `work_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单号',
  `process_id` bigint(0) NOT NULL COMMENT '工序ID',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工序编码',
  `oper_user_id` bigint(0) NOT NULL COMMENT '操作工人ID',
  `oper_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作工人名称',
  `scan_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '扫码计件时间',
  `result_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PASS' COMMENT '工序结果 PASS合格 FAIL不合格',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_work_process`(`work_id`, `process_id`) USING BTREE COMMENT '防止同一工单同一工序重复计件',
  INDEX `idx_work_no`(`work_no`) USING BTREE,
  INDEX `idx_user`(`oper_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单工序计件流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_work_process_record
-- ----------------------------
INSERT INTO `t_work_process_record` VALUES (1, 2, 'WO20260922001', 1, 'CUT', 3, '开料老王', '2026-09-22 23:11:43', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (2, 2, 'WO20260922001', 2, 'ASSEMBLE', 4, '组装小李', '2026-09-22 23:11:43', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (3, 2, 'WO20260922001', 3, 'CUT_NET', 3, '开料老王', '2026-09-22 23:11:43', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (4, 2, 'WO20260922001', 4, 'SAND', 5, '质检阿美', '2026-09-22 23:11:43', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (5, 2, 'WO20260922001', 5, 'CHECK', 6, '打包阿强', '2026-09-22 23:11:44', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (6, 2, 'WO20260922001', 6, 'PACKAGE', 8, '测试2', '2026-09-22 23:11:44', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (7, 2, 'WO20260922001', 7, 'STOCK_IN', 1, '卢超', '2026-09-22 23:11:44', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (8, 3, 'WO20260922002', 1, 'CUT', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (9, 3, 'WO20260922002', 2, 'ASSEMBLE', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (10, 3, 'WO20260922002', 3, 'CUT_NET', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (11, 3, 'WO20260922002', 4, 'SAND', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (12, 3, 'WO20260922002', 5, 'CHECK', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (13, 3, 'WO20260922002', 6, 'PACKAGE', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (14, 3, 'WO20260922002', 7, 'STOCK_IN', 1, '卢超', '2026-09-22 23:53:51', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (15, 4, 'WO20260922003', 1, 'CUT', 1, '卢超', '2026-09-24 13:58:10', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (16, 4, 'WO20260922003', 3, 'CUT_NET', 1, '卢超', '2026-09-24 13:58:10', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (17, 4, 'WO20260922003', 2, 'ASSEMBLE', 1, '卢超', '2026-09-24 13:58:10', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (18, 4, 'WO20260922003', 6, 'PACKAGE', 1, '卢超', '2026-09-24 13:58:10', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (19, 4, 'WO20260922003', 7, 'STOCK_IN', 1, '卢超', '2026-09-24 13:58:10', 'PASS', '');
INSERT INTO `t_work_process_record` VALUES (20, 4, 'WO20260922003', 4, 'SAND', 12, '胡忠平', '2026-09-24 14:35:17', 'PASS', '');

-- ----------------------------
-- Table structure for t_work_report
-- ----------------------------
DROP TABLE IF EXISTS `t_work_report`;
CREATE TABLE `t_work_report`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_order_id` bigint(0) NOT NULL COMMENT '关联生产工单ID t_work_order.id',
  `process_id` bigint(0) NOT NULL COMMENT '工序ID t_process_dict.id',
  `worker_id` bigint(0) NOT NULL COMMENT '报工工人ID sys_user.id',
  `qualified_num` int(0) NOT NULL DEFAULT 0 COMMENT '本次合格完工数量',
  `bad_num` int(0) NOT NULL DEFAULT 0 COMMENT '不良返工数量',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '工序计件单价快照（防止调价影响历史工资）',
  `piece_wage` decimal(12, 2) NOT NULL COMMENT '本次计件工资=合格数量*单价',
  `report_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '报工提交时间',
  `create_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作端IP（PC/小程序）',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_order`(`work_order_id`) USING BTREE,
  INDEX `idx_worker`(`worker_id`) USING BTREE,
  INDEX `idx_process`(`process_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工人扫码报工明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_work_report
-- ----------------------------
INSERT INTO `t_work_report` VALUES (1, 2, 1, 3, 6, 0, 0.80, 4.80, '2026-09-22 23:11:43', '', 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_report` VALUES (2, 2, 2, 4, 6, 0, 2.50, 15.00, '2026-09-22 23:11:43', '', 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_report` VALUES (3, 2, 3, 3, 6, 0, 0.80, 4.80, '2026-09-22 23:11:43', '', 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_report` VALUES (4, 2, 4, 5, 6, 0, 0.50, 3.00, '2026-09-22 23:11:43', '', 1, '2026-09-22 23:11:43', 1, '2026-09-22 23:11:43');
INSERT INTO `t_work_report` VALUES (5, 2, 5, 6, 6, 0, 0.50, 3.00, '2026-09-22 23:11:44', '', 1, '2026-09-22 23:11:44', 1, '2026-09-22 23:11:44');
INSERT INTO `t_work_report` VALUES (6, 2, 6, 8, 6, 0, 1.00, 6.00, '2026-09-22 23:11:44', '', 1, '2026-09-22 23:11:44', 1, '2026-09-22 23:11:44');
INSERT INTO `t_work_report` VALUES (7, 2, 7, 1, 6, 0, 0.50, 3.00, '2026-09-22 23:11:44', '', 1, '2026-09-22 23:11:44', 1, '2026-09-22 23:11:44');
INSERT INTO `t_work_report` VALUES (8, 3, 1, 1, 2, 0, 0.80, 1.60, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (9, 3, 2, 1, 2, 0, 2.50, 5.00, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (10, 3, 3, 1, 2, 0, 0.80, 1.60, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (11, 3, 4, 1, 2, 0, 0.50, 1.00, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (12, 3, 5, 1, 2, 0, 0.50, 1.00, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (13, 3, 6, 1, 2, 0, 1.00, 2.00, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (14, 3, 7, 1, 2, 0, 0.50, 1.00, '2026-09-22 23:53:51', '', 1, '2026-09-22 23:53:51', 1, '2026-09-22 23:53:51');
INSERT INTO `t_work_report` VALUES (15, 4, 1, 1, 1, 0, 0.80, 0.80, '2026-09-24 13:58:10', '', 1, '2026-09-24 13:58:10', 1, '2026-09-24 13:58:10');
INSERT INTO `t_work_report` VALUES (16, 4, 3, 1, 1, 0, 0.80, 0.80, '2026-09-24 13:58:10', '', 1, '2026-09-24 13:58:10', 1, '2026-09-24 13:58:10');
INSERT INTO `t_work_report` VALUES (17, 4, 2, 1, 1, 0, 2.50, 2.50, '2026-09-24 13:58:10', '', 1, '2026-09-24 13:58:10', 1, '2026-09-24 13:58:10');
INSERT INTO `t_work_report` VALUES (18, 4, 6, 1, 1, 0, 1.00, 1.00, '2026-09-24 13:58:10', '', 1, '2026-09-24 13:58:10', 1, '2026-09-24 13:58:10');
INSERT INTO `t_work_report` VALUES (19, 4, 7, 1, 1, 0, 0.50, 0.50, '2026-09-24 13:58:10', '', 1, '2026-09-24 13:58:10', 1, '2026-09-24 13:58:10');
INSERT INTO `t_work_report` VALUES (20, 4, 4, 12, 1, 0, 0.50, 0.50, '2026-09-24 14:35:17', '', 12, '2026-09-24 14:35:17', 12, '2026-09-24 14:35:17');

-- ----------------------------
-- Table structure for t_worker_wage
-- ----------------------------
DROP TABLE IF EXISTS `t_worker_wage`;
CREATE TABLE `t_worker_wage`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_id` bigint(0) NOT NULL COMMENT '工人用户ID sys_user.id',
  `wage_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '薪资月份 格式：2026-08',
  `total_wage` decimal(14, 2) NULL COMMENT '当月总计件工资',
  `deduct_wage` decimal(14, 2) NULL COMMENT '扣款金额',
  `add_wage` decimal(14, 2) NULL COMMENT '补贴/补发金额',
  `real_wage` decimal(14, 2) NULL COMMENT '实发工资=总工资-扣款+补贴',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0未结算 1已结算发放',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注说明',
  `create_by` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_by` bigint(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_worker_month`(`worker_id`, `wage_month`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工人月度薪资汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_worker_wage
-- ----------------------------

-- ----------------------------
-- Table structure for t_wx_user
-- ----------------------------
DROP TABLE IF EXISTS `t_wx_user`;
CREATE TABLE `t_wx_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `sys_user_id` bigint(0) NOT NULL COMMENT '关联系统用户id',
  `nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '微信昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '手机号',
  `session_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信session_key',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid`) USING BTREE,
  INDEX `idx_sys_user`(`sys_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信小程序用户绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_wx_user
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
