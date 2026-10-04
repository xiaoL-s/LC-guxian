# -*- coding: utf-8 -*-
"""写入 拆洗款护童-压条款(110款) 的产品属性 + 公式配置"""
import json
import subprocess

cfg = {
    "vars": [
        {"name": "总高", "sort": 1},
        {"name": "总宽", "sort": 2},
        {"name": "数量", "list": "1,2,3,4,5,6,7,8,9,10", "sort": 3},
        {"name": "颜色", "list": "灰色,白色,咖啡色,黑色,绿色,香槟色", "sort": 4},
        {"name": "网子", "list": "超清网,正宗304高清网,高清网,05黑网,07黑网", "sort": 5},
        {"name": "加杆", "list": "全防护,中间两根", "default": "全防护", "sort": 6},
        {"name": "下固定", "sort": 7},
        {"name": "把手", "list": "金属把手,普通把手,密码把手,专用把手", "sort": 8},
        {"name": "单价", "list": "110", "default": 110, "sort": 9},
        {"name": "把手方向", "list": "左,右,1左1右,2左2右", "sort": 10}
    ],
    "防护杆数量": [
        {"cond": "总高<=1400", "expr": "6"},
        {"cond": "总高>1400", "expr": "8"}
    ],
    "孔位": [
        {"cond": "下固定=0", "expr": "(总高-40)/3"},
        {"cond": "下固定>0", "expr": "(总高-40-下固定)/2"}
    ],
    "型材": {
        "外框高": [{"expr": "总高-28", "mult": "2*数量"}],
        "外框宽": [{"expr": "总宽-28", "mult": "2*数量"}],
        "内框高": [{"expr": "总高-42", "mult": "2*数量"}],
        "内框宽": [{"expr": "总宽-42", "mult": "2*数量"}],
        "内扇高": [{"expr": "孔位+21", "mult": "2*数量"}],
        "内扇宽": [{"expr": "总宽-39", "mult": "2*数量"}],
        "横杆": [{"expr": "内框宽", "mult": "2*数量"}],
        "防护栏": [{"cond": "加杆=全防护", "expr": "总宽-3", "mult": "防护杆数量*数量"}],
        "小短杆": [
            {"cond": "加杆=全防护", "expr": "孔位-36", "mult": "1*数量"},
            {"cond": "加杆=中间两根", "expr": "孔位-36", "mult": "2*数量"}
        ],
        "短横杆": [{"cond": "加杆=全防护", "expr": "总宽-201", "mult": "2*数量"}]
    },
    "纱网": {
        "上网": [{"w": "内框宽+15", "h": "孔位+4", "mult": "1*数量"}],
        "下网": [
            {"cond": "下固定=0", "w": "内框宽+15", "h": "孔位+4", "mult": "1*数量"},
            {"cond": "下固定>0", "w": "内框宽+15", "h": "下固定+4", "mult": "1*数量"}
        ],
        "中网": [{"w": "内框宽-42", "h": "孔位-25", "mult": "1*数量"}]
    },
    "面积": [
        {"cond": "总高*总宽*0.000001<=1", "expr": "1*数量"},
        {"cond": "总高*总宽*0.000001>1", "expr": "总高*总宽*0.000001*数量"}
    ],
    "金额明细": {
        "基本金额": [{"expr": "单价*面积"}]
    }
}

formula_json = json.dumps(cfg, ensure_ascii=False)

# 写入 MySQL（dict_value=9 = 拆洗款护童-压条款(110款)）
sql = ("UPDATE sys_dict_data SET formula_config = %s, update_time = NOW() "
       "WHERE dict_type = 'style_kuangzhongkuang' AND dict_value = '9';")
py_sql = ("import mysql.connector" if False else "")  # placeholder

# 用 mysql CLI 通过临时文件写入（避免命令行转义）
import io, os
tmp = os.path.join(os.path.dirname(__file__), "_fc9.json")
with io.open(tmp, "w", encoding="utf-8") as f:
    f.write(formula_json)

# 生成 SQL 文件
sql_file = os.path.join(os.path.dirname(__file__), "_fc9.sql")
with io.open(sql_file, "w", encoding="utf-8") as f:
    # 用 HEX 方式避免引号转义问题
    f.write("UPDATE sys_dict_data SET formula_config = CONVERT(0x%s USING utf8mb4), update_time = NOW() "
            "WHERE dict_type = 'style_kuangzhongkuang' AND dict_value = '9';\n"
            % formula_json.encode("utf-8").hex())
print("SQL 已生成:", sql_file, "JSON 长度:", len(formula_json))

# 执行
mysql = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
r = subprocess.run([mysql, "--host=127.0.0.1", "--user=root", "--password=root",
                    "--database=guxian-erp", "--default-character-set=utf8mb4",
                    "-e", "source " + sql_file.replace("\\", "/")],
                   capture_output=True, text=True)
print("执行输出:", r.stdout[:200] if r.stdout else "(空)", r.stderr[:200] if r.stderr else "")

# 回读验证
r2 = subprocess.run([mysql, "--host=127.0.0.1", "--user=root", "--password=root",
                     "--database=guxian-erp", "-N",
                     "-e", "SELECT LENGTH(formula_config) FROM sys_dict_data WHERE dict_type='style_kuangzhongkuang' AND dict_value='9';"],
                    capture_output=True, text=True)
print("回读长度:", r2.stdout.strip())
