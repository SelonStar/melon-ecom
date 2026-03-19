ALTER TABLE `tb_sku`
    ADD COLUMN `ai_tryon_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '1=支持 AI 试穿 0=不支持' AFTER `image_url`,
    ADD COLUMN `tryon_category` VARCHAR(32) DEFAULT NULL COMMENT 'TOP/BOTTOM/DRESS/SHOES' AFTER `ai_tryon_enabled`,
    ADD COLUMN `tryon_image_url` VARCHAR(255) DEFAULT NULL COMMENT '试穿专用服饰素材图' AFTER `tryon_category`,
    ADD COLUMN `tryon_mask_url` VARCHAR(255) DEFAULT NULL COMMENT '可选：试穿素材 mask 图' AFTER `tryon_image_url`,
    ADD COLUMN `tryon_sort` INT NOT NULL DEFAULT 0 COMMENT '试穿商品排序值' AFTER `tryon_mask_url`;

CREATE TABLE IF NOT EXISTS `tb_tryon_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `photo_url` VARCHAR(255) NOT NULL,
    `photo_type` VARCHAR(32) NOT NULL COMMENT 'FULL_BODY/HALF_BODY',
    `top_sku_id` BIGINT DEFAULT NULL,
    `bottom_sku_id` BIGINT DEFAULT NULL,
    `dress_sku_id` BIGINT DEFAULT NULL,
    `shoes_sku_id` BIGINT DEFAULT NULL,
    `status` VARCHAR(32) NOT NULL COMMENT 'SUBMITTED/PROCESSING/SUCCESS/FAILED',
    `provider_code` VARCHAR(32) NOT NULL,
    `provider_task_id` VARCHAR(128) DEFAULT NULL,
    `result_image_url` VARCHAR(255) DEFAULT NULL,
    `error_message` VARCHAR(255) DEFAULT NULL,
    `request_snapshot` TEXT DEFAULT NULL,
    `provider_response` TEXT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tryon_user` (`user_id`),
    KEY `idx_tryon_status` (`status`),
    KEY `idx_tryon_provider_task` (`provider_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
