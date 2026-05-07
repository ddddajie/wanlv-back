/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : wanlv

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 07/05/2026 12:59:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `doc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档名称',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `file_suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件后缀',
  `parse_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '解析状态：pending/processing/success/failed',
  `publish_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '发布状态：draft/published/offline',
  `uploaded_by` bigint NULL DEFAULT NULL COMMENT '上传人ID',
  `published_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_parse_status`(`parse_status` ASC) USING BTREE,
  INDEX `idx_publish_status`(`publish_status` ASC) USING BTREE,
  INDEX `idx_uploaded_by`(`uploaded_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识文档管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for map_interaction_log
-- ----------------------------
DROP TABLE IF EXISTS `map_interaction_log`;
CREATE TABLE `map_interaction_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `spot_id` bigint NULL DEFAULT NULL COMMENT '景点ID',
  `route_id` bigint NULL DEFAULT NULL COMMENT '路线ID',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：CLICK_SPOT/VIEW_ROUTE/PLAY_GUIDE/RECOMMEND_ROUTE',
  `action_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发来源：MAP/AGENT/SYSTEM',
  `agent_result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Agent返回结果JSON',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`spot_id` ASC) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地图交互日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_store
-- ----------------------------
DROP TABLE IF EXISTS `message_store`;
CREATE TABLE `message_store`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `session_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scenic_area
-- ----------------------------
DROP TABLE IF EXISTS `scenic_area`;
CREATE TABLE `scenic_area`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景区名称',
  `scenic_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景区编码',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区/县',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景区简介',
  `opening_hours` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开放时间说明',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `cover_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景区封面图',
  `map_base_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景区地图底图URL',
  `map_center_lng` decimal(10, 6) NULL DEFAULT NULL COMMENT '地图中心经度',
  `map_center_lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '地图中心纬度',
  `default_zoom` decimal(5, 2) NULL DEFAULT 15.00 COMMENT '默认缩放级别',
  `min_zoom` decimal(5, 2) NULL DEFAULT 12.00 COMMENT '最小缩放级别',
  `max_zoom` decimal(5, 2) NULL DEFAULT 20.00 COMMENT '最大缩放级别',
  `map_bounds_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '地图边界JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_scenic_code`(`scenic_code` ASC) USING BTREE,
  INDEX `idx_scenic_name`(`scenic_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scenic_geo_feature
-- ----------------------------
DROP TABLE IF EXISTS `scenic_geo_feature`;
CREATE TABLE `scenic_geo_feature`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '所属景区ID',
  `feature_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '要素名称',
  `feature_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '要素类型：BOUNDARY/ZONE/RESTRICTED/ENTRANCE_AREA/ROAD',
  `geometry_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '几何类型：POINT/LINE/POLYGON',
  `feature_sub_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '要素子类型：如WALK/VEHICLE/BOAT/CABLE/OTHER',
  `length_meters` int NULL DEFAULT NULL COMMENT '长度（米），线要素可用',
  `properties_json` json NULL COMMENT '扩展属性JSON',
  `geojson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'GeoJSON数据',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_feature_type`(`feature_type` ASC) USING BTREE,
  INDEX `idx_geometry_type`(`geometry_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景区地图空间要素表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scenic_spot
-- ----------------------------
DROP TABLE IF EXISTS `scenic_spot`;
CREATE TABLE `scenic_spot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '所属景区ID',
  `spot_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景点名称',
  `poi_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SCENIC_SPOT' COMMENT '点位类型：SCENIC_SPOT/TOILET/ENTRANCE/PARKING/SERVICE',
  `icon_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地图图标类型',
  `spot_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景点编码',
  `short_intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景点简述',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景点详细介绍',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `stay_duration_minutes` int NULL DEFAULT NULL COMMENT '建议停留时长（分钟）',
  `opening_hours` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开放时间',
  `cover_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景点封面图',
  `audio_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '讲解音频URL',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '讲解视频URL',
  `knowledge_doc_id` bigint NULL DEFAULT NULL COMMENT '关联知识库文档ID',
  `recommended_level` tinyint NULL DEFAULT 0 COMMENT '推荐等级：0普通 1推荐 2重点推荐',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `reservation_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持预约：0-不支持，1-支持',
  `reservation_notice` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约须知',
  `advance_reservation_days` int NOT NULL DEFAULT 7 COMMENT '最多可提前预约天数',
  `min_advance_minutes` int NOT NULL DEFAULT 30 COMMENT '最少提前预约分钟数',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spot_code`(`spot_code` ASC) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_spot_name`(`spot_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spot_reservation_order
-- ----------------------------
DROP TABLE IF EXISTS `spot_reservation_order`;
CREATE TABLE `spot_reservation_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reservation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约编号',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `spot_id` bigint NOT NULL COMMENT '景点ID',
  `slot_id` bigint NOT NULL COMMENT '预约时段ID',
  `visit_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '预约开始时间',
  `end_time` time NOT NULL COMMENT '预约结束时间',
  `visitor_count` int NOT NULL DEFAULT 1 COMMENT '预约人数',
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人手机号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CONFIRMED' COMMENT '预约状态：PENDING-待确认，CONFIRMED-已预约，CANCELLED-已取消，COMPLETED-已完成，EXPIRED-已过期',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'FRONTEND' COMMENT '预约来源：FRONTEND-前端，AGENT-Agent，ADMIN-后台',
  `agent_session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Agent会话编码',
  `client_request_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端/Agent请求唯一ID，用于防止重复提交',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约备注',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reservation_no`(`reservation_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_client_request_id`(`client_request_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`spot_id` ASC) USING BTREE,
  INDEX `idx_slot_id`(`slot_id` ASC) USING BTREE,
  INDEX `idx_visit_date`(`visit_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_agent_session_code`(`agent_session_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景点预约订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for spot_reservation_rule
-- ----------------------------
DROP TABLE IF EXISTS `spot_reservation_rule`;
CREATE TABLE `spot_reservation_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `spot_id` bigint NOT NULL COMMENT '景点ID',
  `start_time` time NOT NULL COMMENT '预约开始时间',
  `end_time` time NOT NULL COMMENT '预约结束时间',
  `total_capacity` int NOT NULL DEFAULT 0 COMMENT '该时段默认预约容量',
  `week_days` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用星期：1,2,3,4,5,6,7，1表示周一，7表示周日，为空表示每天适用',
  `advance_days` int NOT NULL DEFAULT 7 COMMENT '可提前预约天数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`spot_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景点预约规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for spot_reservation_slot
-- ----------------------------
DROP TABLE IF EXISTS `spot_reservation_slot`;
CREATE TABLE `spot_reservation_slot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `spot_id` bigint NOT NULL COMMENT '景点ID',
  `rule_id` bigint NULL DEFAULT NULL COMMENT '来源预约规则ID，临时时段可为空',
  `visit_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '预约开始时间',
  `end_time` time NOT NULL COMMENT '预约结束时间',
  `total_capacity` int NOT NULL DEFAULT 0 COMMENT '该时段总预约容量',
  `reserved_count` int NOT NULL DEFAULT 0 COMMENT '已预约人数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spot_date_time`(`spot_id` ASC, `visit_date` ASC, `start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`spot_id` ASC) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE,
  INDEX `idx_visit_date`(`visit_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景点预约时段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for spot_reservation_visitor
-- ----------------------------
DROP TABLE IF EXISTS `spot_reservation_visitor`;
CREATE TABLE `spot_reservation_visitor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '预约订单ID',
  `reservation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预约编号',
  `user_id` bigint NOT NULL COMMENT '预约账号ID',
  `scenic_area_id` bigint NOT NULL COMMENT '景区ID',
  `spot_id` bigint NOT NULL COMMENT '景点ID',
  `slot_id` bigint NOT NULL COMMENT '预约时段ID',
  `visit_date` date NOT NULL COMMENT '游玩日期',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '游客真实姓名',
  `id_card_masked` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '脱敏身份证号',
  `id_card_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '身份证哈希',
  `booker` tinyint NOT NULL DEFAULT 0 COMMENT '是否预约本人：0否，1是',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CONFIRMED' COMMENT '占用状态',
  `active_id_card_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci GENERATED ALWAYS AS ((case when (`status` in (_utf8mb4'PENDING',_utf8mb4'CONFIRMED')) then `id_card_hash` else NULL end)) STORED COMMENT '有效占用身份证哈希' NULL,
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_active_identity_spot_date`(`active_id_card_hash` ASC, `spot_id` ASC, `visit_date` ASC) USING BTREE,
  INDEX `idx_reservation_no`(`reservation_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_visit`(`user_id` ASC, `visit_date` ASC) USING BTREE,
  INDEX `idx_identity_visit`(`id_card_hash` ASC, `spot_id` ASC, `visit_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '预约游客实名明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_user`;
CREATE TABLE `sys_admin_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（建议加密存储）',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'admin' COMMENT '角色：admin-管理员，super_admin-超级管理员',
  `scenic_spot` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属于哪个景区',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统管理员用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_normal_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_normal_user`;
CREATE TABLE `sys_normal_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码（游客可为空）',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `interest_tags` json NULL COMMENT '兴趣标签JSON，如[\"历史文化\",\"自然风光\"]',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `real_name_status` tinyint NOT NULL DEFAULT 0 COMMENT '实名状态：0未实名，1已实名，2实名失败',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `id_card_masked` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脱敏身份证号',
  `id_card_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证哈希，用于防重复预约',
  `real_name_time` datetime NULL DEFAULT NULL COMMENT '实名通过时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统普通用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tour_route
-- ----------------------------
DROP TABLE IF EXISTS `tour_route`;
CREATE TABLE `tour_route`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenic_area_id` bigint NOT NULL COMMENT '所属景区ID',
  `route_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路线名称',
  `route_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路线类型：official-官方推荐，history-历史文化，nature-自然风光，family-亲子，elder-老年友好等',
  `suitable_crowd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适合人群说明',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '预计游览时长（分钟）',
  `distance_meters` int NULL DEFAULT NULL COMMENT '路线总距离（米）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '路线说明',
  `recommended_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐理由',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_route_type`(`route_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游览路线表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tour_route_geo
-- ----------------------------
DROP TABLE IF EXISTS `tour_route_geo`;
CREATE TABLE `tour_route_geo`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_id` bigint NOT NULL COMMENT '路线ID',
  `scenic_area_id` bigint NOT NULL COMMENT '所属景区ID',
  `geojson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路线GeoJSON数据',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '路线几何数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tour_route_spot
-- ----------------------------
DROP TABLE IF EXISTS `tour_route_spot`;
CREATE TABLE `tour_route_spot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_id` bigint NOT NULL COMMENT '路线ID',
  `spot_id` bigint NOT NULL COMMENT '景点ID',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '景点顺序号',
  `stay_duration_minutes` int NULL DEFAULT NULL COMMENT '该景点建议停留时长（分钟）',
  `is_must_visit` tinyint NOT NULL DEFAULT 0 COMMENT '是否必游点：0否 1是',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_route_spot`(`route_id` ASC, `spot_id` ASC) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`spot_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '路线景点关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_digital_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_digital_profile`;
CREATE TABLE `user_digital_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '普通用户ID',
  `profile_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '画像名称，如亲子游用户、文化深度游用户',
  `interest_tags` json NULL COMMENT '聚合后的兴趣标签JSON',
  `focus_topics` json NULL COMMENT '高频关注主题JSON',
  `service_needs` json NULL COMMENT '服务需求JSON',
  `knowledge_gaps` json NULL COMMENT '常见知识缺口JSON',
  `travel_style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出游风格',
  `activity_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活跃度等级',
  `sentiment_tendency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '情绪倾向',
  `sentiment_score_avg` decimal(6, 2) NULL DEFAULT NULL COMMENT '平均情绪分',
  `profile_score` int NULL DEFAULT NULL COMMENT '画像完整度分数',
  `source_session_count` int NOT NULL DEFAULT 0 COMMENT '参与聚合的日报会话数',
  `last_analyzed_date` date NULL DEFAULT NULL COMMENT '最近聚合到的日报日期',
  `profile_json` json NULL COMMENT '完整画像JSON快照',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_digital_profile_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_digital_profile_last_analyzed_date`(`last_analyzed_date` ASC) USING BTREE,
  INDEX `idx_user_digital_profile_activity_level`(`activity_level` ASC) USING BTREE,
  INDEX `idx_user_digital_profile_travel_style`(`travel_style` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户数字画像表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for visitor_message
-- ----------------------------
DROP TABLE IF EXISTS `visitor_message`;
CREATE TABLE `visitor_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '所属会话ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `message_no` int NOT NULL COMMENT '会话内消息序号',
  `sender_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送方：visitor/agent/system',
  `message_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text/voice/image/video',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `voice_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '语音转写文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_message_no`(`session_id` ASC, `message_no` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游客聊天记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for visitor_session
-- ----------------------------
DROP TABLE IF EXISTS `visitor_session`;
CREATE TABLE `visitor_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话唯一编码，用于后端与Agent关联会话',
  `user_id` bigint NOT NULL COMMENT '游客/用户ID',
  `user_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '游客昵称',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `report_date` date NOT NULL COMMENT '报告日期',
  `session_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CONSULTATION' COMMENT '会话类型：CONSULTATION/SCENIC_SERVICE',
  `session_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态：ACTIVE/ENDED/ANALYZED',
  `scenic_area_id` bigint NULL DEFAULT NULL COMMENT '景区ID，可为空',
  `scenic_area_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'UNSET' COMMENT '景区来源：UNSET/FRONTEND/USER_CONFIRMED/AGENT_INFERRED/SYSTEM',
  `scenic_area_confirmed` tinyint NOT NULL DEFAULT 0 COMMENT '景区是否已确认：0-未确认，1-已确认',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话入口来源：GLOBAL_CHAT/SCENIC_DETAIL/ROUTE_DETAIL等',
  `source_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源业务ID，如景区ID、路线ID等',
  `attraction_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当天主要游览景点/景区名称',
  `attraction_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景点类型，如主题乐园/博物馆',
  `attraction_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景点介绍或当天关联的景点说明',
  `stay_duration` decimal(10, 2) NULL DEFAULT NULL COMMENT '停留时长（小时）',
  `group_size` int NULL DEFAULT NULL COMMENT '同行人数',
  `ticket_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '门票消费',
  `food_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '餐饮消费',
  `shopping_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '购物消费',
  `transport_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '交通消费',
  `entertainment_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '娱乐消费',
  `total_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总消费',
  `satisfaction` int NULL DEFAULT NULL COMMENT '满意度评分，建议1~5',
  `interaction_count` int NOT NULL DEFAULT 0 COMMENT '当日交互轮次',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Agent生成的当日聊天总结',
  `overall_sentiment` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '整体情感倾向：positive/neutral/negative',
  `sentiment_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '情感评分（0~100）',
  `focus_topics` json NULL COMMENT '关注话题JSON',
  `interest_tags` json NULL COMMENT '兴趣标签JSON',
  `service_suggestions` json NULL COMMENT '服务建议JSON',
  `knowledge_gap_points` json NULL COMMENT '知识缺口JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_code`(`session_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_report_date`(`user_id` ASC, `report_date` ASC) USING BTREE,
  INDEX `idx_report_date`(`report_date` ASC) USING BTREE,
  INDEX `idx_scenic_area_id`(`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_attraction_name`(`attraction_name` ASC) USING BTREE,
  INDEX `idx_session_type`(`session_type` ASC) USING BTREE,
  INDEX `idx_session_status`(`session_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游客日报表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
