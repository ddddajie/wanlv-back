-- 普通用户 refreshToken 增量建表脚本（现有数据库只需执行一次）
CREATE TABLE IF NOT EXISTS `sys_normal_user_refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'refreshToken的SHA-256摘要',
  `user_id` bigint NOT NULL COMMENT '普通用户ID',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `invalidated` tinyint NOT NULL DEFAULT 0 COMMENT '失效状态：0-有效，1-已失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_token_hash` (`token_hash` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_expire_time` (`expire_time` ASC) USING BTREE,
  CONSTRAINT `fk_refresh_token_normal_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_normal_user` (`id`)
    ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '普通用户刷新令牌表'
  ROW_FORMAT = DYNAMIC;
