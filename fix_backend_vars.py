# -*- coding: utf-8 -*-
"""后端：产品字典项携带 formulaConfig，供前端渲染属性下拉与默认值"""
import io

# 1) DictOptionVO 加字段
p1 = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\vo\DictOptionVO.java"
with io.open(p1, "r", encoding="utf-8") as f:
    c1 = f.read()

old1 = '''    /** 排序 */
    private Integer sort;'''
new1 = '''    /** 排序 */
    private Integer sort;
    /** 产品公式配置（产品字典项携带，前端据此渲染属性下拉与默认值） */
    private String formulaConfig;'''
ok1 = old1 in c1
if ok1:
    c1 = c1.replace(old1, new1)
with io.open(p1, "w", encoding="utf-8", newline="\n") as f:
    f.write(c1)

# 2) dictOptions 组装 product 时设置 formulaConfig
p2 = r"D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java\com\guxian\sales\service\impl\TSalesOrderServiceImpl.java"
with io.open(p2, "r", encoding="utf-8") as f:
    c2 = f.read()

old2 = '''            if (type != null && type.startsWith(PRODUCT_DICT_PREFIX)) {
                result.get("product").add(vo);
            } else if (result.containsKey(type)) {'''
new2 = '''            if (type != null && type.startsWith(PRODUCT_DICT_PREFIX)) {
                vo.setFormulaConfig(d.getFormulaConfig());
                result.get("product").add(vo);
            } else if (result.containsKey(type)) {'''
ok2 = old2 in c2
if ok2:
    c2 = c2.replace(old2, new2)
with io.open(p2, "w", encoding="utf-8", newline="\n") as f:
    f.write(c2)

print("DictOptionVO 加 formulaConfig:", ok1)
print("dictOptions 设置 formulaConfig:", ok2)
