DROP DATABASE IF EXISTS melon_ecom;
CREATE DATABASE melon_ecom CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE melon_ecom;

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员 id',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员密码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=170 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户 id',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户邮箱',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户简介',
  `create_time` datetime NOT NULL COMMENT '用户创建时间',
  `update_time` datetime NOT NULL COMMENT '用户修改时间',
  `status` tinyint NOT NULL COMMENT '用户状态：0-启用，1-禁用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username` (`username`) USING BTREE,
  UNIQUE INDEX `email` (`email`) USING BTREE,
  UNIQUE INDEX `phone` (`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=148 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_warehouse`;
CREATE TABLE `tb_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库 id',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码（唯一）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
  `city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
  `district` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区/县',
  `address_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code` (`code`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

-- ----------------------------
-- Table structure for tb_banner
-- ----------------------------
DROP TABLE IF EXISTS `tb_banner`;
CREATE TABLE `tb_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'banner id',
  `title` varchar(128) DEFAULT NULL COMMENT '标题',
  `banner_url` varchar(255) NOT NULL COMMENT '图片 url',
  `link_url` varchar(255) DEFAULT NULL COMMENT '点击跳转链接（可到商品/分类/活动）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间（可选）',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间（可选）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_banner_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 插入一条启用轮播图（status=1）
INSERT INTO tb_banner (title, banner_url, link_url, sort, status)
VALUES ('Pocket 3',
        'http://localhost:9000/melon-ecom-data/banner/dji_osmo_pocket3_banner_1920x600.png',
        '/product/1',
        1,
        1);

DROP TABLE IF EXISTS `tb_home_nav_card`;
CREATE TABLE `tb_home_nav_card` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '首页导航卡 id',
  `title` varchar(128) NOT NULL COMMENT '导航卡标题',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_home_nav_card_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_home_nav_card_item`;
CREATE TABLE `tb_home_nav_card_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '首页导航卡商品位 id',
  `card_id` bigint NOT NULL COMMENT '所属导航卡 id',
  `keyword` varchar(128) NOT NULL COMMENT '点击后搜索关键词',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品 id，可为空',
  `title` varchar(128) NOT NULL COMMENT '商品位标题',
  `image_url` varchar(255) NOT NULL COMMENT '商品位图片',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_home_nav_card_item_card` (`card_id`),
  KEY `idx_home_nav_card_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `tb_home_nav_card_item`
ADD COLUMN IF NOT EXISTS `keyword` varchar(128) NOT NULL DEFAULT '' COMMENT '点击后搜索关键词' AFTER `card_id`;

INSERT INTO `tb_home_nav_card` (`title`, `sort`) VALUES
('热销电子产品', 1),
('电脑办公', 2),
('家居好物', 3),
('时尚潮玩', 4);

INSERT INTO `tb_home_nav_card_item` (`card_id`, `keyword`, `product_id`, `title`, `image_url`, `sort`) VALUES
(1, '手机数码', NULL, '手机数码', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=640&q=80', 1),
(1, '智能穿戴', NULL, '智能穿戴', 'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=640&q=80', 2),
(1, '音频设备', NULL, '音频设备', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=640&q=80', 3),
(1, '电脑配件', NULL, '电脑配件', 'https://images.unsplash.com/photo-1517336714739-489689fd1ca8?auto=format&fit=crop&w=640&q=80', 4),
(2, '笔记本', NULL, '笔记本', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=640&q=80', 1),
(2, '办公外设', NULL, '办公外设', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=640&q=80', 2),
(2, '显示设备', NULL, '显示设备', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=640&q=80', 3),
(2, '桌面收纳', NULL, '桌面收纳', 'https://images.unsplash.com/photo-1587831990711-23ca6441447b?auto=format&fit=crop&w=640&q=80', 4),
(3, '卧室焕新', NULL, '卧室焕新', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=640&q=80', 1),
(3, '客厅布置', NULL, '客厅布置', 'https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=640&q=80', 2),
(3, '收纳整理', NULL, '收纳整理', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=640&q=80', 3),
(3, '厨房精选', NULL, '厨房精选', 'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=640&q=80', 4),
(4, '潮流服饰', NULL, '潮流服饰', 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=640&q=80', 1),
(4, '潮鞋精选', NULL, '潮鞋精选', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=640&q=80', 2),
(4, '配饰箱包', NULL, '配饰箱包', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=640&q=80', 3),
(4, '热门潮玩', NULL, '热门潮玩', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=640&q=80', 4);

DROP TABLE IF EXISTS `tb_stock`;
CREATE TABLE `tb_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存记录 id',
  `warehouse_id` bigint NOT NULL COMMENT '仓库 id',
  `sku_id` bigint NOT NULL COMMENT 'sku id',
  `available` int NOT NULL DEFAULT 0 COMMENT '可售库存',
  `locked` int NOT NULL DEFAULT 0 COMMENT '锁定库存（占用未扣减）',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wh_sku` (`warehouse_id`, `sku_id`) USING BTREE,
  INDEX `idx_sku` (`sku_id`) USING BTREE,
  INDEX `idx_warehouse` (`warehouse_id`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_stock_reservation`;
CREATE TABLE `tb_stock_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '锁定记录 id',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库 id',
  `sku_id` bigint NOT NULL COMMENT 'sku id',
  `qty` int NOT NULL COMMENT '锁定数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-LOCKED 2-RELEASED 3-DEDUCTED 4-FAILED',
  `expire_at` datetime NULL DEFAULT NULL COMMENT '锁定过期时间（用于超时释放）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_sku` (`order_no`, `sku_id`) USING BTREE,
  INDEX `idx_order_no` (`order_no`) USING BTREE,
  INDEX `idx_wh_sku` (`warehouse_id`, `sku_id`) USING BTREE,
  INDEX `idx_status_expire` (`status`, `expire_at`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_warehouse_allocation`;
CREATE TABLE `tb_warehouse_allocation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分仓记录 id',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `sku_id` bigint NOT NULL COMMENT 'sku id',
  `warehouse_id` bigint NOT NULL COMMENT '仓库 id',
  `qty` int NOT NULL COMMENT '该仓分配数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_sku` (`order_no`, `sku_id`) USING BTREE,
  INDEX `idx_order_no` (`order_no`) USING BTREE,
  INDEX `idx_wh` (`warehouse_id`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_stock_txn`;
CREATE TABLE `tb_stock_txn` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存流水 id',
  `biz_type` tinyint NOT NULL COMMENT '业务类型：1-LOCK 2-RELEASE 3-DEDUCT 4-ADJUST 5-INBOUND 6-OUTBOUND',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务单号（订单号/调整单号等）',
  `warehouse_id` bigint NOT NULL COMMENT '仓库 id',
  `sku_id` bigint NOT NULL COMMENT 'sku id',
  `delta_available` int NOT NULL DEFAULT 0 COMMENT 'available 变化量（可正可负）',
  `delta_locked` int NOT NULL DEFAULT 0 COMMENT 'locked 变化量（可正可负）',
  `before_available` int NULL DEFAULT NULL COMMENT '变更前 available（可选）',
  `before_locked` int NULL DEFAULT NULL COMMENT '变更前 locked（可选）',
  `after_available` int NULL DEFAULT NULL COMMENT '变更后 available（可选）',
  `after_locked` int NULL DEFAULT NULL COMMENT '变更后 locked（可选）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_biz` (`biz_no`, `biz_type`) USING BTREE,
  INDEX `idx_wh_sku` (`warehouse_id`, `sku_id`) USING BTREE,
  INDEX `idx_sku` (`sku_id`) USING BTREE
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_brand`;
CREATE TABLE `tb_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌 id',
  `name` varchar(64) NOT NULL COMMENT '品牌名',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '品牌 logo',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_brand_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_category`;
CREATE TABLE `tb_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类 id',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类 id，0 表示根',
  `name` varchar(64) NOT NULL COMMENT '分类名',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_category_parent` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 分类初始数据
INSERT INTO `tb_category` (`parent_id`, `name`, `sort`, `status`) VALUES
(0, '手机数码', 1, 1),
(0, '电脑办公', 2, 1),
(0, '家用电器', 3, 1),
(0, '服装鞋帽', 4, 1),
(0, '时尚潮玩', 5, 1);

DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品 id（SPU）',
  `name` varchar(128) NOT NULL COMMENT '商品名',
  `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌 id',
  `category_id` bigint DEFAULT NULL COMMENT '分类 id',
  `main_image_url` varchar(255) DEFAULT NULL COMMENT '主图',
  `detail_html` text COMMENT '详情描述（富文本）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_brand` (`brand_id`) USING BTREE,
  KEY `idx_product_category` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `tb_product`
ADD COLUMN `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT '评论数（仅统计可见主评论）' AFTER `status`,
ADD COLUMN `rating_avg` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均评分（0.00 ~ 5.00）' AFTER `comment_count`;

ALTER TABLE `tb_product`
ADD COLUMN `sales_count` BIGINT NOT NULL DEFAULT 0 COMMENT '销量' AFTER `rating_avg`;

DROP TABLE IF EXISTS `tb_product_image`;
CREATE TABLE `tb_product_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片 id',
  `product_id` bigint NOT NULL COMMENT '商品 id',
  `url` varchar(255) NOT NULL COMMENT '图片 url',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_image_product` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_sku`;
CREATE TABLE `tb_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'sku id',
  `product_id` bigint NOT NULL COMMENT '商品 id（SPU）',
  `sku_code` varchar(64) NOT NULL COMMENT 'sku 编码（唯一）',
  `name` varchar(128) NOT NULL COMMENT 'sku 名（如：红色/XL）',
  `spec_json` varchar(1024) DEFAULT NULL COMMENT '规格 json（如：{"color":"red","size":"XL"}）',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `weight` decimal(10,3) DEFAULT NULL COMMENT '重量（kg）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sku_code` (`sku_code`) USING BTREE,
  KEY `idx_sku_product` (`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `tb_sku`
ADD COLUMN `stock` INT NOT NULL DEFAULT 0 COMMENT '库存' AFTER `price`;

ALTER TABLE `tb_sku`
ADD COLUMN `image_url` varchar(255) DEFAULT NULL COMMENT 'sku 图片（可选）' AFTER `spec_json`;

ALTER TABLE `tb_sku`
ADD COLUMN `sales_count` BIGINT NOT NULL DEFAULT 0 COMMENT '销量';

DROP TABLE IF EXISTS `tb_product_comment`;
CREATE TABLE `tb_product_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论 id',
  `product_id` bigint NOT NULL COMMENT '商品 id(SPU)',
  `sku_id` bigint DEFAULT NULL COMMENT 'sku id（可选）',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父评论 id，0=主评论',
  `root_id` bigint NOT NULL DEFAULT 0 COMMENT '根评论 id，0=主评论（用于楼中楼查询）',
  `rating` tinyint DEFAULT NULL COMMENT '评分 1-5（仅主评论有意义）',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1显示 0隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_comment_product` (`product_id`) USING BTREE,
  KEY `idx_comment_user` (`user_id`) USING BTREE,
  KEY `idx_comment_parent` (`parent_id`) USING BTREE,
  KEY `idx_comment_root` (`root_id`) USING BTREE,
  KEY `idx_comment_status_time` (`status`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

DROP TABLE IF EXISTS `tb_user_wallet`;
CREATE TABLE `tb_user_wallet` (
  `user_id` bigint NOT NULL COMMENT '用户id=tb_user.id',
  `balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_cart_item`;
CREATE TABLE `tb_cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `sku_id` bigint NOT NULL,
  `quantity` int NOT NULL COMMENT '数量>=1',
  `checked` tinyint NOT NULL DEFAULT 1 COMMENT '1勾选 0未勾选',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_cart_user` (`user_id`),
  KEY `idx_cart_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` varchar(64) NOT NULL COMMENT '订单号（唯一）',
  `user_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已取消',
  `total_amount` decimal(18,2) NOT NULL COMMENT '订单总额',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_order_item`;
CREATE TABLE `tb_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `sku_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL COMMENT '后端自动确定仓库（默认单仓，从库存表取，前端不传）',
  `quantity` int NOT NULL,
  `unit_price` decimal(18,2) NOT NULL COMMENT '下单时单价快照',
  `amount` decimal(18,2) NOT NULL COMMENT 'unit_price*quantity',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_item_order` (`order_id`),
  KEY `idx_item_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `tb_pay_txn`;
CREATE TABLE `tb_pay_txn` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  `pay_type` tinyint NOT NULL COMMENT '1余额',
  `amount` decimal(18,2) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0处理中 1成功 2失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_order` (`order_no`),
  KEY `idx_pay_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;