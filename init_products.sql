-- 1. 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_time, del_flag)
SELECT * FROM (
  SELECT '产品分类' AS a, 'product_type' AS b, 1 AS c, NOW() AS d, 0 AS e UNION ALL
  SELECT '产品名称', 'product_name', 1, NOW(), 0 UNION ALL
  SELECT '颜色', 'product_color', 1, NOW(), 0 UNION ALL
  SELECT '纱网材质', 'net_material', 1, NOW(), 0
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type s WHERE s.dict_type = t.b);

-- 2. 产品分类
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'product_type' a, '隐形纱窗系列' b, '隐形纱窗' c, 1 d, 1 e, NOW() f, 0 g UNION ALL
  SELECT 'product_type', '纱门系列', '纱门', 2, 1, NOW(), 0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 3. 产品名称（12个）
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'product_name' a, '32小方盒 固定' b, '32小方盒固定' c, 1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'product_name', '42大方盒 固定', '42大方盒固定', 2,1,NOW(),0 UNION ALL
  SELECT 'product_name', '32平框 固定', '32平框固定', 3,1,NOW(),0 UNION ALL
  SELECT 'product_name', '32平框不锈钢护栏 固定', '32平框护栏固定', 4,1,NOW(),0 UNION ALL
  SELECT 'product_name', '32平框拆洗', '32平框拆洗', 5,1,NOW(),0 UNION ALL
  SELECT 'product_name', '32平框不锈钢护栏拆洗', '32平框护栏拆洗', 6,1,NOW(),0 UNION ALL
  SELECT 'product_name', '断桥隐纱', '断桥隐纱', 7,1,NOW(),0 UNION ALL
  SELECT 'product_name', '断桥隐纱不锈钢护栏', '断桥隐纱护栏', 8,1,NOW(),0 UNION ALL
  SELECT 'product_name', '8K32平框焊接拆洗', '8K32平框焊接拆洗', 9,1,NOW(),0 UNION ALL
  SELECT 'product_name', '52纱门', '52纱门', 10,1,NOW(),0 UNION ALL
  SELECT 'product_name', '82纱门', '82纱门', 11,1,NOW(),0 UNION ALL
  SELECT 'product_name', '旗舰款50纱门', '旗舰款50纱门', 12,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 4. 颜色
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'product_color' a,'金属灰' b,'金属灰' c,1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'product_color','金属咖啡','金属咖啡',2,1,NOW(),0 UNION ALL
  SELECT 'product_color','白色','白色',3,1,NOW(),0 UNION ALL
  SELECT 'product_color','黑金','黑金',4,1,NOW(),0 UNION ALL
  SELECT 'product_color','肌肤黑','肌肤黑',5,1,NOW(),0 UNION ALL
  SELECT 'product_color','黑色','黑色',6,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 5. 纱网材质
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'net_material' a,'玻纤网' b,'玻纤网' c,1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'net_material','柔性金刚网','柔性金刚网',2,1,NOW(),0 UNION ALL
  SELECT 'net_material','304柔钢网','304柔钢网',3,1,NOW(),0 UNION ALL
  SELECT 'net_material','防宠物网','防宠物网',4,1,NOW(),0 UNION ALL
  SELECT 'net_material','70纱盒','70纱盒',5,1,NOW(),0 UNION ALL
  SELECT 'net_material','100纱盒','100纱盒',6,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 6. 产品档案 t_product（编码 gx001 起）
INSERT INTO t_product (product_code, product_name, product_type, spec, unit, unit_price, price_type, min_area, default_color, default_material, status, remark, create_time, del_flag)
SELECT * FROM (
  SELECT 'gx001' a,'32小方盒 固定' b,'隐形纱窗' c,'玻纤网/金属灰/咖啡/白/黑金' d,'米' e,78.00 f,2 g,1.0000 h,'金属灰' i,'玻纤网' j,1 k,'标配拉手；加锁+10元/米' l, NOW() m,0 n UNION ALL
  SELECT 'gx002','42大方盒 固定','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',108.00,2,1.0000,'金属灰','玻纤网',1,'加锁+10元/米',NOW(),0 UNION ALL
  SELECT 'gx003','32平框 固定','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',108.00,2,1.0000,'金属灰','玻纤网',1,'',NOW(),0 UNION ALL
  SELECT 'gx004','32平框不锈钢护栏 固定','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',148.00,2,1.0000,'金属灰','玻纤网',1,'304柔钢网+10元/米',NOW(),0 UNION ALL
  SELECT 'gx005','32平框拆洗','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',148.00,2,1.0000,'金属灰','玻纤网',1,'尺寸未达一米按一米计算',NOW(),0 UNION ALL
  SELECT 'gx006','32平框不锈钢护栏拆洗','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',188.00,2,1.0000,'金属灰','玻纤网',1,'最大高1600×宽750；纱盒<1米只能加锁',NOW(),0 UNION ALL
  SELECT 'gx007','断桥隐纱','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',148.00,2,1.0000,'金属灰','玻纤网',1,'',NOW(),0 UNION ALL
  SELECT 'gx008','断桥隐纱不锈钢护栏','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',188.00,2,1.0000,'金属灰','玻纤网',1,'',NOW(),0 UNION ALL
  SELECT 'gx009','8K32平框焊接拆洗','隐形纱窗','玻纤网/金属灰/咖啡/白/黑金','米',188.00,2,1.0000,'金属灰','玻纤网',1,'不锈钢护栏固定在外框，纱网单独拆',NOW(),0 UNION ALL
  SELECT 'gx010','52纱门','纱门','玻纤网/金属灰/咖啡/白/肌肤黑','㎡',228.00,1,2.0000,'金属灰','玻纤网',1,'防宠物网+20元/平方',NOW(),0 UNION ALL
  SELECT 'gx011','82纱门','纱门','玻纤网/金属灰/咖啡/白/肌肤黑','㎡',248.00,1,2.0000,'金属灰','玻纤网',1,'单开2平方起算，对开4平方起算',NOW(),0 UNION ALL
  SELECT 'gx012','旗舰款50纱门','纱门','玻纤网/金属灰/咖啡/白/黑','㎡',488.00,1,2.0000,'金属灰','玻纤网',1,'可选配70纱盒/100纱盒',NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM t_product s WHERE s.product_code = t.a);

SELECT '字典类型数' AS k, COUNT(*) AS v FROM sys_dict_type WHERE dict_type IN ('product_type','product_name','product_color','net_material')
UNION ALL
SELECT '字典数据数', COUNT(*) FROM sys_dict_data WHERE dict_type LIKE 'product%' OR dict_type='net_material'
UNION ALL
SELECT '产品档案数', COUNT(*) FROM t_product WHERE product_code LIKE 'gx%';
