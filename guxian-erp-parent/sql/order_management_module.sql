-- =============================================================================
-- 订单管理模块 数据库变更脚本
-- 目标库：guxian-erp
-- 说明：
--   1) 扩展 t_sales_order（订单主表）：订单类型、品牌、单位、安装方式、设计师/拆单师/
--      业务员、物流、终端地址、客户来源、投影面积、大板数、财务状态、已付/实收金额、
--      利润与毛利率、子单数量等；
--   2) 扩展 t_sales_order_item（产品明细，即"子单"行）：子单号、品目/字典、网子、把手、
--      锁具、把手方向、加杆、下固定、方板、扣宽与净宽、计算方式、行状态、行利润/实收、
--      以及工序/生产流程/生产进度等"生产预留"字段；
--   3) 调整销售订单管理菜单，新增"订单明细"菜单。
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 -D guxian-erp < order_management_module.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 订单主表扩展
-- -----------------------------------------------------------------------------
ALTER TABLE t_sales_order
  ADD COLUMN order_type       varchar(20)   NOT NULL DEFAULT '正常单' COMMENT '订单类型：正常单/加急单/经销商单/样品单' AFTER order_status,
  ADD COLUMN brand            varchar(50)   NULL     DEFAULT NULL   COMMENT '品牌' AFTER remark,
  ADD COLUMN unit             varchar(20)   NULL     DEFAULT '套'   COMMENT '单位' AFTER brand,
  ADD COLUMN install_type     varchar(30)   NULL     DEFAULT '客户安装' COMMENT '安装方式' AFTER unit,
  ADD COLUMN designer         varchar(50)   NULL     DEFAULT NULL   COMMENT '设计师' AFTER install_type,
  ADD COLUMN splitter         varchar(50)   NULL     DEFAULT NULL   COMMENT '拆单师' AFTER designer,
  ADD COLUMN salesman         varchar(50)   NULL     DEFAULT NULL   COMMENT '业务员' AFTER splitter,
  ADD COLUMN logistics        varchar(50)   NULL     DEFAULT NULL   COMMENT '物流' AFTER salesman,
  ADD COLUMN terminal_address varchar(255)  NULL     DEFAULT NULL   COMMENT '终端地址' AFTER logistics,
  ADD COLUMN customer_source  varchar(50)   NULL     DEFAULT NULL   COMMENT '客户来源' AFTER terminal_address,
  ADD COLUMN projection_area  decimal(12,4) NOT NULL DEFAULT 0.0000 COMMENT '投影面积(㎡)' AFTER total_area,
  ADD COLUMN big_board_num    int           NOT NULL DEFAULT 0      COMMENT '大板数' AFTER projection_area,
  ADD COLUMN finance_status   tinyint       NOT NULL DEFAULT 0      COMMENT '财务状态：0未收账 1部分已收账 2已收账 3已结清' AFTER total_amount,
  ADD COLUMN paid_amount      decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '已付金额' AFTER finance_status,
  ADD COLUMN receive_amount   decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '实收金额(累计)' AFTER paid_amount,
  ADD COLUMN unpaid_amount    decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '未付金额' AFTER receive_amount,
  ADD COLUMN profit_amount    decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '利润' AFTER unpaid_amount,
  ADD COLUMN gross_profit_rate decimal(8,4) NOT NULL DEFAULT 0.0000 COMMENT '毛利率' AFTER profit_amount,
  ADD COLUMN sub_order_count  int           NOT NULL DEFAULT 0      COMMENT '子单数量' AFTER gross_profit_rate,
  ADD COLUMN receive_time     datetime      NULL     DEFAULT NULL   COMMENT '最近收款时间' AFTER sub_order_count;

-- -----------------------------------------------------------------------------
-- 2. 订单明细（子单）表扩展
-- -----------------------------------------------------------------------------
ALTER TABLE t_sales_order_item
  ADD COLUMN sub_order_no     varchar(40)   NULL     DEFAULT NULL   COMMENT '子单号' AFTER order_id,
  ADD COLUMN item_category    varchar(50)   NULL     DEFAULT NULL   COMMENT '品目(字典系列名称)' AFTER product_type,
  ADD COLUMN dict_type        varchar(50)   NULL     DEFAULT NULL   COMMENT '产品字典类型 style_xxx' AFTER item_category,
  ADD COLUMN unit             varchar(20)   NULL     DEFAULT '套'   COMMENT '单位' AFTER num,
  ADD COLUMN net_material     varchar(50)   NULL     DEFAULT NULL   COMMENT '网子' AFTER color,
  ADD COLUMN handle           varchar(50)   NULL     DEFAULT NULL   COMMENT '把手' AFTER net_material,
  ADD COLUMN lock_set         varchar(50)   NULL     DEFAULT NULL   COMMENT '锁具' AFTER handle,
  ADD COLUMN handle_direction varchar(20)   NULL     DEFAULT NULL   COMMENT '把手方向' AFTER lock_set,
  ADD COLUMN add_rod          varchar(20)   NULL     DEFAULT NULL   COMMENT '加杆' AFTER handle_direction,
  ADD COLUMN fixed_bottom     varchar(20)   NULL     DEFAULT NULL   COMMENT '下固定' AFTER add_rod,
  ADD COLUMN square_board     varchar(50)   NULL     DEFAULT NULL   COMMENT '方板规格(5号方板等)' AFTER fixed_bottom,
  ADD COLUMN deduct_width     decimal(10,2) NOT NULL DEFAULT 0.00   COMMENT '扣宽(mm)' AFTER height,
  ADD COLUMN net_width        decimal(10,2) NULL     DEFAULT NULL   COMMENT '扣宽后净宽(mm)，参与面积计算' AFTER deduct_width,
  ADD COLUMN calc_type        tinyint       NOT NULL DEFAULT 1      COMMENT '计算方式：1按面积 2按件' AFTER min_area,
  ADD COLUMN item_status      tinyint       NOT NULL DEFAULT 0      COMMENT '行状态：0订单未受理 1订单已受理 2生产中 3已完工 4已发货 5已完成 9已取消' AFTER line_amount,
  ADD COLUMN sale_price_type  tinyint       NOT NULL DEFAULT 1      COMMENT '销售计价方式：1按面积 2按件 3按公式' AFTER item_status,
  ADD COLUMN freight          decimal(12,2) NOT NULL DEFAULT 0.00   COMMENT '行运费' AFTER sale_price_type,
  ADD COLUMN receive_amount   decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '行实收金额' AFTER freight,
  ADD COLUMN profit_amount    decimal(14,2) NOT NULL DEFAULT 0.00   COMMENT '行利润' AFTER receive_amount,
  ADD COLUMN sales_owner      varchar(50)   NULL     DEFAULT NULL   COMMENT '销售商/下单员' AFTER profit_amount,
  ADD COLUMN process_code     varchar(50)   NULL     DEFAULT NULL   COMMENT '当前工序编码(生产预留)' AFTER sales_owner,
  ADD COLUMN process_name     varchar(50)   NULL     DEFAULT NULL   COMMENT '当前工序名称(生产预留)' AFTER process_code,
  ADD COLUMN flow_status      varchar(50)   NULL     DEFAULT NULL   COMMENT '生产流程(生产预留)' AFTER process_name,
  ADD COLUMN produce_progress int           NOT NULL DEFAULT 0      COMMENT '生产进度%(生产预留)' AFTER flow_status,
  ADD COLUMN work_order_id    bigint        NULL     DEFAULT NULL   COMMENT '关联生产工单ID(生产预留)' AFTER produce_progress;

-- 子单号查询索引
ALTER TABLE t_sales_order_item
  ADD INDEX idx_sub_order_no (sub_order_no),
  ADD INDEX idx_item_status (item_status);

-- -----------------------------------------------------------------------------
-- 3. 放宽历史非空约束
--    产品改为字典驱动后，明细行的 product_type 允许为空（后端会用"品目"兜底）
-- -----------------------------------------------------------------------------
ALTER TABLE t_sales_order_item
  MODIFY COLUMN product_type varchar(50) NULL DEFAULT NULL COMMENT '产品类型/分类（快照，字典驱动后可空）';

-- -----------------------------------------------------------------------------
-- 4. 销售订单管理菜单调整
-- -----------------------------------------------------------------------------
UPDATE sys_menu SET menu_name = '下单', path = '/sales/order/entry', perms = 'sales:order:add', sort = 2 WHERE id = 402;
UPDATE sys_menu SET menu_name = '订单列表', path = '/sales/order/list', perms = 'sales:order:list', sort = 3 WHERE id = 403;
UPDATE sys_menu SET menu_name = '订单明细', path = '/sales/order/items', perms = 'sales:order:item', sort = 4 WHERE id = 404;
DELETE FROM sys_menu WHERE id = 405;
INSERT INTO sys_menu (id, parent_id, menu_name, path, perms, menu_type, sort, del_flag, icon)
VALUES (405, 40, '订单进度跟踪', '/sales/order/track', 'sales:track:view', 2, 5, 0, NULL);

-- -----------------------------------------------------------------------------
-- 5. 销售订单管理菜单授权
--    角色 1=超级管理员、2=文员(下单岗)、15=既有销售角色，统一授予 401~405
-- -----------------------------------------------------------------------------
DELETE FROM sys_role_menu WHERE role_id = 2 AND menu_id BETWEEN 401 AND 405;
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (2, 401), (2, 402), (2, 403), (2, 404), (2, 405);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 405), (15, 405);
