CREATE TABLE IF NOT EXISTS `agent_route_geo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `scenic_area_id` bigint NOT NULL COMMENT '所属景区ID',
  `route_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路线名称',
  `geojson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路线GeoJSON数据',
  `spot_ids_json` json NULL COMMENT '景点ID顺序JSON',
  `spot_names_json` json NULL COMMENT '景点名称顺序JSON',
  `distance_meters` decimal(12, 2) NULL DEFAULT NULL COMMENT '路线距离，单位米',
  `spot_count` int NOT NULL DEFAULT 0 COMMENT '景点数量',
  `road_segment_count` int NOT NULL DEFAULT 0 COMMENT '道路片段数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_scenic_area_id` (`scenic_area_id` ASC) USING BTREE,
  INDEX `idx_create_time` (`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent用户定制路线几何数据表' ROW_FORMAT = Dynamic;
