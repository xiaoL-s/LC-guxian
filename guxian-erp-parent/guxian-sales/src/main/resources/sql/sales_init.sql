-- ============================================================
-- 销售订单管理模块 建表/补字段脚本（guxian-sales，端口8083）
-- 库：guxian-erp
-- 宽高口径：宽(width)、高(height) 单位毫米(mm)，面积单位平方米(㎡)
--   单扇面积 single_area = width * height / 1,000,000
--   计费面积 charge_area = max(single_area, 最小起算方 min_area)
--   行总面积 item_total_area = charge_area * num
--   行金额 line_amount = item_total_area * unit_price
-- ============================================================

-- ------------------------------------------------------------
-- 1. 产品主表 t_product
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_product` (
  `product_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_code`   varchar(64)  NOT NULL COMMENT '产品编码',
  `product_name`   varchar(100) NOT NULL COMMENT '产品名称',
  `product_type`   varchar(50)  DEFAULT NULL COMMENT '产品分类：平开纱窗/推拉纱窗/金刚网纱窗/折叠纱窗',
  `spec`           varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit`           varchar(20)  DEFAULT '㎡' COMMENT '计价单位',
  `unit_price`     decimal(12,2) DEFAULT 0.00 COMMENT '基础单价（元/㎡或元/件）',
  `price_type`     tinyint      DEFAULT 1 COMMENT '计价方式：1按面积 2按件',
  `min_area`       decimal(10,4) DEFAULT 0.0000 COMMENT '最小起算方（㎡），单扇面积不足按此计',
  `default_color`  varchar(30)  DEFAULT NULL COMMENT '默认颜色',
  `default_material` varchar(30) DEFAULT NULL COMMENT '默认材质',
  `open_direction` varchar(30)  DEFAULT NULL COMMENT '默认开启方向',
  `status`         tinyint      DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark`         varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by`      bigint       DEFAULT NULL COMMENT '创建人',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      bigint       DEFAULT NULL COMMENT '更新人',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`       tinyint      DEFAULT 0 COMMENT '删除标记：0正常 1删除',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案表';

-- ------------------------------------------------------------
-- 2. 销售订单主表 t_sales_order 补字段
-- ------------------------------------------------------------
ALTER TABLE `t_sales_order`
  ADD COLUMN `order_status`    tinyint      DEFAULT 0 COMMENT '订单状态：0待审核 1已审核(待排产) 2生产中 3已完工 4已发货 5已完成 6已驳回 7已取消' AFTER `order_no`,
  ADD COLUMN `customer_name`   varchar(100) DEFAULT NULL COMMENT '客户名称（冗余，便于列表展示）' AFTER `customer_id`,
  ADD COLUMN `contact`         varchar(50)  DEFAULT NULL COMMENT '联系人' AFTER `customer_name`,
  ADD COLUMN `phone`           varchar(30)  DEFAULT NULL COMMENT '联系电话' AFTER `contact`,
  ADD COLUMN `address`         varchar(255) DEFAULT NULL COMMENT '安装/收货地址' AFTER `phone`,
  ADD COLUMN `order_date`      date         DEFAULT NULL COMMENT '下单日期' AFTER `address`,
  ADD COLUMN `expect_date`     date         DEFAULT NULL COMMENT '期望交期' AFTER `order_date`,
  ADD COLUMN `product_amount`  decimal(14,2) DEFAULT 0.00 COMMENT '产品金额合计（明细行金额之和）' AFTER `total_area`,
  ADD COLUMN `craft_fee`       decimal(12,2) DEFAULT 0.00 COMMENT '特殊工艺加价' AFTER `product_amount`,
  ADD COLUMN `urgent_fee`      decimal(12,2) DEFAULT 0.00 COMMENT '加急费' AFTER `craft_fee`,
  ADD COLUMN `freight`         decimal(12,2) DEFAULT 0.00 COMMENT '运费' AFTER `urgent_fee`,
  ADD COLUMN `discount_amount` decimal(12,2) DEFAULT 0.00 COMMENT '优惠/减价' AFTER `freight`,
  ADD COLUMN `total_amount`    decimal(14,2) DEFAULT 0.00 COMMENT '订单总金额' AFTER `discount_amount`,
  ADD COLUMN `audit_by`        bigint       DEFAULT NULL COMMENT '审核人' AFTER `total_amount`,
  ADD COLUMN `audit_time`      datetime     DEFAULT NULL COMMENT '审核时间' AFTER `audit_by`,
  ADD COLUMN `reject_reason`   varchar(255) DEFAULT NULL COMMENT '驳回原因' AFTER `audit_time`,
  ADD COLUMN `delivery_time`   datetime     DEFAULT NULL COMMENT '发货时间' AFTER `reject_reason`,
  ADD COLUMN `finish_time`     datetime     DEFAULT NULL COMMENT '完成时间' AFTER `delivery_time`,
  ADD COLUMN `cancel_reason`   varchar(255) DEFAULT NULL COMMENT '取消原因' AFTER `finish_time`;

-- ------------------------------------------------------------
-- 3. 销售订单明细表 t_sales_order_item 补字段
-- ------------------------------------------------------------
ALTER TABLE `t_sales_order_item`
  ADD COLUMN `product_id`     bigint        DEFAULT NULL COMMENT '产品ID' AFTER `order_id`,
  ADD COLUMN `product_name`   varchar(100)  DEFAULT NULL COMMENT '产品名称（快照）' AFTER `product_id`,
  ADD COLUMN `material`       varchar(30)   DEFAULT NULL COMMENT '材质' AFTER `color`,
  ADD COLUMN `open_direction` varchar(30)   DEFAULT NULL COMMENT '开启方向' AFTER `material`,
  ADD COLUMN `unit_price`     decimal(12,2) DEFAULT 0.00 COMMENT '单价（元/㎡，快照）' AFTER `open_direction`,
  ADD COLUMN `min_area`       decimal(10,4) DEFAULT 0.0000 COMMENT '最小起算方（㎡，快照）' AFTER `single_area`,
  ADD COLUMN `charge_area`    decimal(12,4) DEFAULT 0.0000 COMMENT '单扇计费面积=max(单扇面积,最小起算方)' AFTER `min_area`,
  ADD COLUMN `line_amount`    decimal(14,2) DEFAULT 0.00 COMMENT '行金额=行总面积×单价' AFTER `item_total_area`;

-- ------------------------------------------------------------
-- 4. 产品BOM表 t_product_bom 补逻辑删除（与全局规范对齐，可选）
--    原表无 del_flag，补充以便逻辑删除
-- ------------------------------------------------------------
ALTER TABLE `t_product_bom`
  ADD COLUMN `del_flag` tinyint DEFAULT 0 COMMENT '删除标记：0正常 1删除';

-- ------------------------------------------------------------
-- 5. 初始化示例产品（便于联调，可删除）
-- ------------------------------------------------------------
INSERT INTO `t_product`(`product_code`,`product_name`,`product_type`,`spec`,`unit`,`unit_price`,`price_type`,`min_area`,`default_color`,`default_material`,`open_direction`,`status`,`remark`)
SELECT 'P001','金刚网防盗纱窗(平开)','金刚网纱窗','按㎡定制','㎡',280.00,1,0.5000,'灰色','304不锈钢金刚网','外开',1,'含五金配件'
WHERE NOT EXISTS (SELECT 1 FROM `t_product` WHERE `product_code`='P001');
INSERT INTO `t_product`(`product_code`,`product_name`,`product_type`,`spec`,`unit`,`unit_price`,`price_type`,`min_area`,`default_color`,`default_material`,`open_direction`,`status`,`remark`)
SELECT 'P002','普通推拉纱窗','推拉纱窗','按㎡定制','㎡',120.00,1,0.3000,'白色','玻璃纤维网','左右推拉',1,'常规款'
WHERE NOT EXISTS (SELECT 1 FROM `t_product` WHERE `product_code`='P002');
INSERT INTO `t_product`(`product_code`,`product_name`,`product_type`,`spec`,`unit`,`unit_price`,`price_type`,`min_area`,`default_color`,`default_material`,`open_direction`,`status`,`remark`)
SELECT 'P003','折叠隐形纱窗','折叠纱窗','按㎡定制','㎡',160.00,1,0.3000,'灰色','聚酯纤维网','折叠',1,'折叠收纳'
WHERE NOT EXISTS (SELECT 1 FROM `t_product` WHERE `product_code`='P003');
