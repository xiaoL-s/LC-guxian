-- 清理之前误插的字典
DELETE FROM sys_dict_data WHERE dict_type IN ('product_type','product_name','product_color','net_material');
DELETE FROM sys_dict_type WHERE dict_type IN ('product_type','product_name','product_color','net_material');

-- 1. sys_dict_type 新增两个产品系列
INSERT INTO sys_dict_type (dict_type, dict_name, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'yixing_shachuang' a, '隐形纱窗系列' b, 1 c, NOW() d, 0 e
  UNION ALL SELECT 'shamen', '纱门系列', 1, NOW(), 0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type s WHERE s.dict_type = t.a);

-- 2. sys_dict_data 隐形纱窗系列（9个）
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'yixing_shachuang' a, '32小方盒 固定' b, '32小方盒固定' c, 1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'yixing_shachuang','42大方盒 固定','42大方盒固定',2,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','32平框 固定','32平框固定',3,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','32平框不锈钢护栏 固定','32平框护栏固定',4,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','32平框拆洗','32平框拆洗',5,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','32平框不锈钢护栏拆洗','32平框护栏拆洗',6,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','断桥隐纱','断桥隐纱',7,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','断桥隐纱不锈钢护栏','断桥隐纱护栏',8,1,NOW(),0 UNION ALL
  SELECT 'yixing_shachuang','8K32平框焊接拆洗','8K32平框焊接拆洗',9,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 3. sys_dict_data 纱门系列（3个）
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'shamen' a, '52纱门' b, '52纱门' c, 1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'shamen','82纱门','82纱门',2,1,NOW(),0 UNION ALL
  SELECT 'shamen','旗舰款50纱门','旗舰款50纱门',3,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 4. 颜色字典
INSERT INTO sys_dict_type (dict_type, dict_name, status, create_time, del_flag)
SELECT * FROM (SELECT 'product_color' a, '产品颜色' b, 1 c, NOW() d, 0 e) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type s WHERE s.dict_type=t.a);
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'product_color' a,'金属灰' b,'金属灰' c,1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'product_color','金属咖啡','金属咖啡',2,1,NOW(),0 UNION ALL
  SELECT 'product_color','白色','白色',3,1,NOW(),0 UNION ALL
  SELECT 'product_color','黑金','黑金',4,1,NOW(),0 UNION ALL
  SELECT 'product_color','肌肤黑','肌肤黑',5,1,NOW(),0 UNION ALL
  SELECT 'product_color','黑色','黑色',6,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 5. 纱网材质字典
INSERT INTO sys_dict_type (dict_type, dict_name, status, create_time, del_flag)
SELECT * FROM (SELECT 'net_material' a, '纱网材质' b, 1 c, NOW() d, 0 e) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type s WHERE s.dict_type=t.a);
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, create_time, del_flag)
SELECT * FROM (
  SELECT 'net_material' a,'玻纤网' b,'玻纤网' c,1 d,1 e,NOW() f,0 g UNION ALL
  SELECT 'net_material','柔性金刚网','柔性金刚网',2,1,NOW(),0 UNION ALL
  SELECT 'net_material','304柔钢网','304柔钢网',3,1,NOW(),0 UNION ALL
  SELECT 'net_material','防宠物网','防宠物网',4,1,NOW(),0 UNION ALL
  SELECT 'net_material','70纱盒','70纱盒',5,1,NOW(),0 UNION ALL
  SELECT 'net_material','100纱盒','100纱盒',6,1,NOW(),0
) t WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data s WHERE s.dict_type=t.a AND s.dict_label=t.b);

-- 验证
SELECT '类型数' k, COUNT(*) v FROM sys_dict_type WHERE del_flag=0
UNION ALL SELECT '隐形纱窗产品数', COUNT(*) FROM sys_dict_data WHERE dict_type='yixing_shachuang'
UNION ALL SELECT '纱门产品数', COUNT(*) FROM sys_dict_data WHERE dict_type='shamen'
UNION ALL SELECT '颜色数', COUNT(*) FROM sys_dict_data WHERE dict_type='product_color'
UNION ALL SELECT '纱网数', COUNT(*) FROM sys_dict_data WHERE dict_type='net_material';
