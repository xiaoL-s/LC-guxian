package com.guxian.system.formula;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 公式表达式求值器回归用例。
 *
 * <p>锁住老系统口径：<code>=</code> 当相等比较、中文变量按长度降序替换（防子串误替换）、
 * 裸中文当字符串字面量、逗号多条件按 AND、{@code jw()} 进一取整、{@code lwxs()} 恒等、
 * 未传入的可选变量按 0 参与运算与比较。</p>
 *
 * <p>纯 JUnit 用例，不加载 Spring 上下文，不依赖数据库。</p>
 */
class FormulaExprEvaluatorTest {

    /** 快速构造上下文：ctx("总高", 600.0, "总宽", 350.0) */
    private static Map<String, Object> ctx(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static double num(String expr, Map<String, Object> c) {
        return FormulaExprEvaluator.evalNum(expr, c);
    }

    @Test
    @DisplayName("算术运算：优先级与括号")
    void arithmeticAndPrecedence() {
        Map<String, Object> c = ctx("总高", 600.0, "总宽", 350.0);
        assertEquals(560.0, num("总高-40", c), 1e-9);
        assertEquals(351.0, num("总宽+1", c), 1e-9);
        assertEquals(355.0, num("总宽+10/2", c), 1e-9, "乘除优先于加减");
        assertEquals(180.0, num("(总宽+10)/2", c), 1e-9, "括号改变优先级");
        assertEquals(702.0, num("(总宽+1)*2", c), 1e-9);
        assertEquals(186.66666666666666, num("(总高-40)/3", c), 1e-6);
    }

    @Test
    @DisplayName("一元负号与小数常量")
    void unaryAndDecimal() {
        assertEquals(-350.5, num("-总宽", ctx("总宽", 350.5)), 1e-9);
        assertEquals(0.21, num("350*600*0.000001", ctx()), 1e-9);
    }

    @Test
    @DisplayName("= 视为相等比较（老系统口径）")
    void equalsAsComparison() {
        assertTrue(FormulaExprEvaluator.evalBool("下固定=0", ctx("下固定", 0.0)));
        assertFalse(FormulaExprEvaluator.evalBool("下固定=0", ctx("下固定", 100.0)));
        assertTrue(FormulaExprEvaluator.evalBool("加杆=全防护", ctx("加杆", "全防护")));
        assertFalse(FormulaExprEvaluator.evalBool("加杆=全防护", ctx("加杆", "中间两根")));
        // 未传入的可选变量按 0 比较：下固定没填就走「等于 0」分支
        assertTrue(FormulaExprEvaluator.evalBool("下固定=0", ctx()));
        assertFalse(FormulaExprEvaluator.evalBool("下固定>0", ctx()));
    }

    @Test
    @DisplayName("<= / >= / != 不被 = 转义破坏")
    void comparisonOperators() {
        assertTrue(FormulaExprEvaluator.evalBool("总高<=1400", ctx("总高", 600.0)));
        assertFalse(FormulaExprEvaluator.evalBool("总高<=1400", ctx("总高", 1401.0)));
        assertTrue(FormulaExprEvaluator.evalBool("总高>=1400", ctx("总高", 1400.0)));
        assertTrue(FormulaExprEvaluator.evalBool("网子!=05黑网", ctx("网子", "高清网")));
        assertFalse(FormulaExprEvaluator.evalBool("网子!=05黑网", ctx("网子", "05黑网")));
    }

    @Test
    @DisplayName("jw 进一取整、lwxs 恒等")
    void functions() {
        assertEquals(2.0, num("jw(1.61)", ctx()), 1e-9, "不足整按 1 计");
        assertEquals(2.0, num("jw(2)", ctx()), 1e-9, "整值不进位");
        assertEquals(3.0, num("jw(2.0001)", ctx()), 1e-9);
        assertEquals(1.61, num("lwxs(1.61)", ctx()), 1e-9);
        // 老系统超高加价写法：jw((总高-1399)/100)*5，总高 1600 → jw(2.01)=3 → 15
        assertEquals(15.0, num("jw((总高-1399)/100)*5", ctx("总高", 1600.0)), 1e-9);
    }

    @Test
    @DisplayName("中文变量按长度降序替换，防子串误替换")
    void variableBoundary() {
        // 只定义了「高」，不能把「总高」里的子串换掉
        assertEquals("总高", FormulaExprEvaluator.eval("总高", ctx("高", 600.0)));
        // 长名优先：定义了 把手 与 密码把手 时，密码把手 先替换
        assertTrue(FormulaExprEvaluator.evalBool("密码把手=右", ctx("把手", "左", "密码把手", "右")));
        // 字符串变量替换后参与相等比较
        assertTrue(FormulaExprEvaluator.evalBool("把手方向=左", ctx("把手方向", "左")));
    }

    @Test
    @DisplayName("整数值替换为整数文本，避免出现 350.0 这类多余小数")
    void numberFormatting() {
        assertEquals(351.0, num("总宽+1", ctx("总宽", 350.0)), 1e-9);
        assertEquals(2.0, num("2*数量", ctx("数量", 1.0)), 1e-9);
        assertEquals(21.0, num("面积*100", ctx("面积", 0.21)), 1e-9);
    }

    @Test
    @DisplayName("逗号分隔的多条件为 AND")
    void commaIsAnd() {
        assertTrue(FormulaExprEvaluator.evalBool("单价>0,下固定=0", ctx("单价", 198.0)));
        assertFalse(FormulaExprEvaluator.evalBool("单价>0,下固定=0", ctx("单价", 198.0, "下固定", 100.0)));
        assertTrue(FormulaExprEvaluator.evalBool("超高>0,lwxs(总高*总宽*0.000001)<=1",
                ctx("超高", 5.0, "总高", 600.0, "总宽", 350.0)));
    }

    @Test
    @DisplayName("未传入的可选变量在算术中按 0 处理")
    void missingVariableIsZero() {
        assertEquals(5.0, num("下固定+5", ctx()), 1e-9);
        assertEquals(0.0, num("单价*面积", ctx()), 1e-9);
    }

    @Test
    @DisplayName("除零、非法字符、多余内容均抛 IllegalArgumentException")
    void invalidExpressions() {
        assertThrows(IllegalArgumentException.class, () -> num("总高/0", ctx("总高", 600.0)),
                "除数为 0 必须报错，不能返回 Infinity");
        assertThrows(IllegalArgumentException.class,
                () -> FormulaExprEvaluator.eval("总高 总宽", ctx("总高", 600.0)),
                "表达式多余内容必须报错");
        assertThrows(IllegalArgumentException.class, () -> FormulaExprEvaluator.eval("总高#2", ctx("总高", 600.0)),
                "非法字符必须报错");
    }
}
