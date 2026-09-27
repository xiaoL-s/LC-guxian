-- =============================================================================
-- 生产单（工单打印）默认模板
-- 生成来源：guxian-erp-screen/src/utils/cuttingSheetRenderer.ts -> buildDefaultBlocks()
-- 说明：打印模板管理与「生产 → 生产工单 → 打印生产单」共用这一条模板
--       （按 template_type='WORK_ORDER' + is_default=1 读取），改这里两边同时生效。
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 -D guxian-erp < production_sheet_template.sql
-- =============================================================================

-- 1. 若不存在则插入默认生产单模板
INSERT INTO t_print_template (template_name, template_type, paper_size, template_content, is_default, status, create_by, create_time, update_by, update_time, del_flag)
SELECT '生产下料单', 'WORK_ORDER', 'A4_LANDSCAPE', '[{"id":1,"type":"qrcode","x":15,"y":10,"w":76,"label":"设备扫码"},{"id":2,"type":"text","x":15,"y":90,"w":100,"fontSize":10,"label":"打印二维码"},{"id":3,"type":"text","x":230,"y":14,"w":620,"fontSize":19,"bold":true,"label":"固贤纱窗纱门生产单","field":"specTitle"},{"id":4,"type":"qrcode","x":992,"y":10,"w":76,"label":"客户二维码"},{"id":5,"type":"text","x":110,"y":104,"w":260,"fontSize":12,"label":"客户名称：","field":"customerName"},{"id":6,"type":"text","x":110,"y":126,"w":260,"fontSize":12,"label":"客户地址：","field":"customerAddress"},{"id":7,"type":"text","x":420,"y":104,"w":280,"fontSize":12,"label":"订单编号：","field":"orderNo"},{"id":8,"type":"text","x":420,"y":126,"w":280,"fontSize":12,"label":"工程地址：","field":"projectAddress"},{"id":9,"type":"text","x":720,"y":104,"w":300,"fontSize":12,"label":"交货日期：","field":"deliverDate"},{"id":10,"type":"text","x":720,"y":126,"w":300,"fontSize":12,"label":"产品名称：","field":"productName"},{"id":11,"type":"line","x":15,"y":150,"w":1050},{"id":12,"type":"table","x":15,"y":162,"w":1050,"cols":["总宽×总高","数量","颜色","固定","外框宽×高","内框宽×高","内扇宽×高","孔位","横杆","纱网","上下纱宽×高","中纱宽×高","加杆","把手","备注"],"colsText":"总宽×总高,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,上下纱宽×高,中纱宽×高,加杆,把手,备注"}]', 1, 1, 1, NOW(), 1, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM t_print_template WHERE template_name = '生产下料单' AND template_type = 'WORK_ORDER' AND del_flag = 0
);

-- 2. 已存在则覆盖为最新版式
UPDATE t_print_template
SET template_content = '[{"id":1,"type":"qrcode","x":15,"y":10,"w":76,"label":"设备扫码"},{"id":2,"type":"text","x":15,"y":90,"w":100,"fontSize":10,"label":"打印二维码"},{"id":3,"type":"text","x":230,"y":14,"w":620,"fontSize":19,"bold":true,"label":"固贤纱窗纱门生产单","field":"specTitle"},{"id":4,"type":"qrcode","x":992,"y":10,"w":76,"label":"客户二维码"},{"id":5,"type":"text","x":110,"y":104,"w":260,"fontSize":12,"label":"客户名称：","field":"customerName"},{"id":6,"type":"text","x":110,"y":126,"w":260,"fontSize":12,"label":"客户地址：","field":"customerAddress"},{"id":7,"type":"text","x":420,"y":104,"w":280,"fontSize":12,"label":"订单编号：","field":"orderNo"},{"id":8,"type":"text","x":420,"y":126,"w":280,"fontSize":12,"label":"工程地址：","field":"projectAddress"},{"id":9,"type":"text","x":720,"y":104,"w":300,"fontSize":12,"label":"交货日期：","field":"deliverDate"},{"id":10,"type":"text","x":720,"y":126,"w":300,"fontSize":12,"label":"产品名称：","field":"productName"},{"id":11,"type":"line","x":15,"y":150,"w":1050},{"id":12,"type":"table","x":15,"y":162,"w":1050,"cols":["总宽×总高","数量","颜色","固定","外框宽×高","内框宽×高","内扇宽×高","孔位","横杆","纱网","上下纱宽×高","中纱宽×高","加杆","把手","备注"],"colsText":"总宽×总高,数量,颜色,固定,外框宽×高,内框宽×高,内扇宽×高,孔位,横杆,纱网,上下纱宽×高,中纱宽×高,加杆,把手,备注"}]',
    paper_size = 'A4_LANDSCAPE',
    status = 1,
    is_default = 1,
    update_time = NOW()
WHERE template_name = '生产下料单' AND template_type = 'WORK_ORDER' AND del_flag = 0;

-- 3. 同一类型下只保留一个默认模板（其它置为非默认，避免打印取错）
UPDATE t_print_template
SET is_default = 0, update_time = NOW()
WHERE template_type = 'WORK_ORDER' AND del_flag = 0
  AND template_id <> (SELECT template_id FROM (SELECT template_id FROM t_print_template
      WHERE template_type = 'WORK_ORDER' AND del_flag = 0 ORDER BY is_default DESC, template_id DESC LIMIT 1) t);
