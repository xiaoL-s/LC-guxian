package com.guxian.system.formula;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 产品公式计算器回归用例。
 *
 * <p>核心基线来自线上真实订单（20261110X0001「8K口袋锁护童款-不锈钢款」350×600）的 formula_result，
 * 公式配置取自 sys_dict_data.formula_config 原样导出，用于锁住：
 * 变量组四舍五入取整、型材/纱网向上取整、型材行间互相引用、变量组取整值回写下游、
 * 金额明细同名多行累加、空串按未填、vars 列表默认值。</p>
 *
 * <p>纯 JUnit 用例，不加载 Spring 上下文，不连数据库。</p>
 */
class ProductFormulaCalculatorTest {

    private static String resource(String name) throws IOException {
        try (InputStream in = ProductFormulaCalculatorTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(in, "测试资源缺失: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** 快速构造输入参数：input("总宽", 350, "总高", 600) */
    private static Map<String, Object> input(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static double num(Object o) {
        assertNotNull(o, "期望有值，实际为 null");
        return ((Number) o).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> child(Map<String, Object> m, String k) {
        Object o = m.get(k);
        assertNotNull(o, "缺少分组: " + k);
        return (Map<String, Object>) o;
    }

    /** 校验一行型材的 dim / mult / total */
    private static void assertSize(Map<String, Object> out, String name, double dim, double mult, double total) {
        Map<String, Object> row = child(child(out, "型材"), name);
        assertEquals(dim, num(row.get("dim")), 1e-9, name + " dim");
        assertEquals(mult, num(row.get("mult")), 1e-9, name + " mult");
        assertEquals(total, num(row.get("total")), 1e-9, name + " total");
    }

    @Test
    @DisplayName("真实产品回归：8K口袋锁护童款-不锈钢款 350×600 与线上订单结果逐项一致")
    void realProductRegression() throws Exception {
        String json = resource("formula/pocket-lock-350x600.json");
        Map<String, Object> out = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1, "下固定", 0));

        assertEquals(6.0, num(out.get("防护杆数量")), 1e-9, "总高<=1400 应为 6 根");
        assertEquals(187.0, num(out.get("孔位")), 1e-9, "(600-40)/3=186.67 四舍五入为 187");

        assertSize(out, "外框高", 601, 2, 1202);
        assertSize(out, "外框宽", 351, 2, 702);
        assertSize(out, "内框高", 574, 2, 1148);
        assertSize(out, "内框宽", 289, 2, 578);
        assertSize(out, "内扇高", 644, 1, 644);
        assertSize(out, "内扇宽", 350, 2, 700);

        Map<String, Object> net = child(child(out, "纱网"), "中网");
        assertEquals(249.0, num(net.get("w")), 1e-9, "中纱宽 = 内框宽-40");
        assertEquals(534.0, num(net.get("h")), 1e-9, "中纱高 = 内框高-40");
        assertEquals(1.0, num(net.get("mult")), 1e-9);

        assertEquals(1.0, num(out.get("面积")), 1e-9, "0.21 平方不足 1 按 1 平方起算");
        assertEquals(198.0, num(child(out, "金额明细").get("基本金额")), 1e-9);
        assertEquals(198.0, num(out.get("总金额")), 1e-9);
    }

    @Test
    @DisplayName("真实产品回归：大料三节 800×1200，型材引用取整后的孔位")
    void realDaliaoSanjieRegression() throws Exception {
        String json = resource("formula/daliao-sanjie.json");
        Map<String, Object> out = ProductFormulaCalculator.calc(json,
                input("总宽", 800, "总高", 1200, "数量", 1, "下固定", 0));

        assertEquals(6.0, num(out.get("防护杆数量")), 1e-9);
        assertEquals(389.0, num(out.get("孔位")), 1e-9, "(1200-40)/3+2=388.67 四舍五入为 389");

        assertSize(out, "外框高", 1160, 2, 2320);
        assertSize(out, "外框宽", 760, 2, 1520);
        assertSize(out, "横杆", 760, 2, 1520);
        assertSize(out, "内扇宽", 763, 2, 1526);
        // 内扇高 = 孔位+18，引用的是取整后的 389（不是 388.67），故为 407
        assertSize(out, "内扇高", 407, 2, 814);

        Map<String, Object> up = child(child(out, "纱网"), "上网");
        assertEquals(776.0, num(up.get("w")), 1e-9, "外框宽+16");
        assertEquals(394.0, num(up.get("h")), 1e-9, "孔位+5");

        Map<String, Object> middle = child(child(out, "纱网"), "中网");
        assertEquals(722.0, num(middle.get("w")), 1e-9, "内扇宽-41");
        assertEquals(370.0, num(middle.get("h")), 1e-9, "孔位-19");

        assertEquals(1.0, num(out.get("面积")), 1e-9, "0.96 平方不足 1 按 1 平方起算");
        assertEquals(85.0, num(out.get("总金额")), 1e-9, "单价默认 85 × 面积 1");
    }

    @Test
    @DisplayName("下固定有值时孔位改走 (总高-40-下固定)/2 分支")
    void fixedBottomBranch() throws Exception {
        String json = resource("formula/pocket-lock-350x600.json");
        Map<String, Object> out = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1, "下固定", 100));
        assertEquals(230.0, num(out.get("孔位")), 1e-9, "(600-40-100)/2=230");
    }

    @Test
    @DisplayName("空串参数按未填处理，与完全不传等价（不按 0 参与分支判断）")
    void emptyStringTreatedAsAbsent() throws Exception {
        String json = resource("formula/pocket-lock-350x600.json");
        Map<String, Object> absent = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1));
        Map<String, Object> empty = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1, "下固定", "", "加杆", ""));

        assertEquals(num(absent.get("孔位")), num(empty.get("孔位")), 1e-9);
        assertEquals(absent.get("型材").toString(), empty.get("型材").toString(), "空串不应让配件行出现");
        assertEquals(num(absent.get("总金额")), num(empty.get("总金额")), 1e-9);
    }

    @Test
    @DisplayName("加杆=全防护 时输出防护栏/小短杆/短横杆，且短横杆引用防护栏原值")
    void fullProtectionAccessories() throws Exception {
        String json = resource("formula/pocket-lock-350x600.json");
        Map<String, Object> out = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1, "加杆", "全防护"));

        assertSize(out, "防护栏", 325, 6, 1950);   // 总宽-25，数量=防护杆数量*数量
        assertSize(out, "小短杆", 240, 1, 240);
        assertSize(out, "短横杆", 125, 1, 125);    // 防护栏-200
        assertFalse(child(out, "型材").containsKey("加杆"), "加杆=中间两根 的行不该出现");
    }

    @Test
    @DisplayName("vars 带 list 的变量未传时取列表首个值，传了就按传入值算")
    void defaultFromVarsList() throws Exception {
        String json = resource("formula/pocket-lock-350x600.json");
        Map<String, Object> def = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1));
        assertEquals(198.0, num(child(def, "金额明细").get("基本金额")), 1e-9, "单价默认取 list 首个 198");

        Map<String, Object> passed = ProductFormulaCalculator.calc(json,
                input("总宽", 350, "总高", 600, "数量", 1, "单价", 158));
        assertEquals(158.0, num(child(passed, "金额明细").get("基本金额")), 1e-9, "显式传参优先");
        assertEquals(158.0, num(passed.get("总金额")), 1e-9);
    }

    @Test
    @DisplayName("型材可引用其它型材，且不依赖 JSON 键顺序")
    void profileCrossReference() throws Exception {
        String json = """
                {
                  "vars": [{"name": "总宽"}],
                  "型材": {
                    "横杆": [{"expr": "内框宽"}],
                    "内框宽": [{"expr": "总宽-61", "mult": "2"}],
                    "内框宽副本": [{"expr": "横杆", "mult": "1"}]
                  }
                }
                """;
        Map<String, Object> out = ProductFormulaCalculator.calc(json, input("总宽", 350));
        assertSize(out, "内框宽", 289, 2, 578);
        assertSize(out, "横杆", 289, 1, 289);
        assertSize(out, "内框宽副本", 289, 1, 289);
    }

    @Test
    @DisplayName("取整口径：变量组四舍五入，型材与纱网向上取整")
    void roundingRules() throws Exception {
        String json = """
                {
                  "vars": [{"name": "总高"}, {"name": "总宽"}],
                  "孔位": [{"expr": "(总高-40)/3"}],
                  "型材": {"横条": [{"expr": "总宽/3", "mult": "0.4"}]},
                  "纱网": {"中网": [{"w": "总宽/3", "h": "(总宽/3)+0.2", "mult": "1.5"}]},
                  "面积": [{"expr": "总宽*总高*0.000001"}]
                }
                """;
        Map<String, Object> out = ProductFormulaCalculator.calc(json, input("总宽", 350, "总高", 601));

        assertEquals(187.0, num(out.get("孔位")), 1e-9, "561/3=187 整数");
        // 350/3 = 116.67 → dim 进一 117；total = 116.67*0.4 = 46.67 → 进一 47
        assertSize(out, "横条", 117, 0.4, 47);

        Map<String, Object> net = child(child(out, "纱网"), "中网");
        assertEquals(117.0, num(net.get("w")), 1e-9);
        assertEquals(117.0, num(net.get("h")), 1e-9, "116.87 进一");
        assertEquals(1.5, num(net.get("mult")), 1e-9);
        assertEquals(0.02, num(net.get("area")), 1e-9, "平米保留两位");

        assertEquals(0.21, num(out.get("面积")), 1e-9);
    }

    @Test
    @DisplayName("金额明细同名多行满足条件时全部累加")
    void amountRowsAccumulate() throws Exception {
        String json = """
                {
                  "vars": [{"name": "总高"}],
                  "金额明细": {
                    "加价": [
                      {"expr": "10", "cond": "总高>0"},
                      {"expr": "5", "cond": "总高>100"},
                      {"expr": "20", "cond": "总高>1500"}
                    ]
                  }
                }
                """;
        Map<String, Object> out = ProductFormulaCalculator.calc(json, input("总高", 600));
        assertEquals(15.0, num(child(out, "金额明细").get("加价")), 1e-9, "10+5，第三档不满足");
        assertEquals(15.0, num(out.get("总金额")), 1e-9);
    }

    @Test
    @DisplayName("花括号 {a,b} 条件按 AND 处理")
    void braceConditionIsAnd() throws Exception {
        String json = """
                {
                  "vars": [{"name": "总宽"}, {"name": "总高"}],
                  "型材": {"特殊条": [{"expr": "总宽+5", "cond": "{总宽>300,总高>500}"}]}
                }
                """;
        Map<String, Object> hit = ProductFormulaCalculator.calc(json, input("总宽", 350, "总高", 600));
        assertSize(hit, "特殊条", 355, 1, 355);

        Map<String, Object> miss = ProductFormulaCalculator.calc(json, input("总宽", 300, "总高", 600));
        assertFalse(child(miss, "型材").containsKey("特殊条"), "300>300 不成立，整行不出");
    }

    @Test
    @DisplayName("公式除零时抛出异常，而不是算出 Infinity 悄悄进生产单")
    void divideByZeroFailsFast() {
        String json = """
                {"vars": [{"name": "总高"}], "孔位": [{"expr": "总高/0"}]}
                """;
        assertThrows(IllegalArgumentException.class,
                () -> ProductFormulaCalculator.calc(json, input("总高", 600)));
    }
}
