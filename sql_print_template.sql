-- =====================================================================
-- 打印模板管理：补齐纸张大小、状态字段
-- 表：t_print_template
-- 说明：对应后端 SysPrintTemplate / TPrintTemplate 实体
-- =====================================================================

-- 1. 新增字段（如已存在请忽略报错或按需调整）
ALTER TABLE t_print_template
    ADD COLUMN paper_size VARCHAR(20) NULL COMMENT '纸张大小：A4 / A5 / 80mm小票' AFTER template_type,
    ADD COLUMN status     TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=禁用' AFTER is_default;

-- 2. 初始化历史数据：旧数据默认 A4 + 启用
UPDATE t_print_template SET paper_size = 'A4' WHERE paper_size IS NULL OR paper_size = '';
UPDATE t_print_template SET status     = 1     WHERE status IS NULL;

-- 3. 索引：按单据类型 + 状态查询
CREATE INDEX idx_pt_type_status ON t_print_template (template_type, status);

-- =====================================================================
-- 可选：插入图1 中的三条示例数据（如表为空可执行）
-- =====================================================================
-- INSERT INTO t_print_template
--   (template_name, template_type, paper_size, template_content, is_default, status, create_time)
-- VALUES
-- ('客户订单单A4', '销售订单', 'A4',
-- '========== 纱窗订单单 ==========\n订单编号：${orderNo}\n客户名称：${customerName}\n联系电话：${customerPhone}\n下单时间：${createTime}\n\n--------------------------------\n纱窗明细：\n产品：高清纱窗\n尺寸：宽 ${width}mm × 高 ${height}mm\n面积：${area}m²    单价：${price}元/m²\n金额：${totalMoney} 元\n\n备注：${remark}\n\n【二维码区域】${qrcode}\n\n--------------------------------\n制单人：${createUser}',
-- 1, 1, NOW()),
-- ('生产派工单', '生产派工单', 'A4',
-- '========== 生产派工单 ==========\n工单号：${orderNo}\n制单人：${createUser}',
-- 1, 1, NOW()),
-- ('简易订单小票', '销售订单', '80mm小票',
-- '订单：${orderNo}\n客户：${customerName}\n金额：${totalMoney}',
-- 0, 0, NOW());
