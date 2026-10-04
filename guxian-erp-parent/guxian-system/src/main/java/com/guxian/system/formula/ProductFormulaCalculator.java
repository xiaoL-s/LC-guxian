package com.guxian.system.formula;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 产品公式计算器：按「变量组 → 型材 → 纱网 → 面积 → 金额明细(累加)」顺序计算。
 *
 * <p>配置结构对应 sys_dict_data.formula_config（公式管理页结构化 JSON），口径与前端公式引擎原型一致：
 * <ul>
 *   <li>变量组（如 防护杆数量/孔位）：顶层数组，每行 {cond, expr}，取第一个满足条件的行；</li>
 *   <li>型材：{名称: 行|行数组}，行 {cond, expr(尺寸), mult(数量)}，取第一满足行，dim 暴露为变量供后续引用；
 *       <b>支持行间互相引用（如 横杆=内框宽）</b>，采用多轮循环计算，不依赖 JSON 字段顺序；</li>
 *   <li>纱网：{名称: 行|行数组}，行 {cond, w, h, mult}；</li>
 *   <li>面积：数组行 {cond, expr}，取第一满足行；</li>
 *   <li>金额明细：{名称: 行|行数组}，行 {cond, expr}，所有满足行累加（总高加价两档叠加）。</li>
 * </ul>
 * </p>
 */
public final class ProductFormulaCalculator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> FIXED_GROUPS = new HashSet<>(
            Arrays.asList("vars", "productName", "型材", "纱网", "面积", "金额明细"));

    /** 一行公式 */
    private static final class Row {
        String cond; // 可为 null
        String expr; // 变量/面积/金额
        String mult; // 型材数量
        String w;    // 纱网宽
        String h;    // 纱网高
    }

    /**
     * 执行计算
     *
     * @param formulaJson 产品公式配置 JSON（sys_dict_data.formula_config）
     * @param input       输入属性参数（总高/总宽/数量/下固定/加杆/单价/把手…，数字或字符串）
     * @return 计算结果：变量、型材、纱网、面积、金额明细、总金额
     */
    public static Map<String, Object> calc(String formulaJson, Map<String, Object> input) throws Exception {
        JsonNode cfg = MAPPER.readTree(formulaJson);
        Map<String, Object> ctx = new HashMap<>();
        for (Map.Entry<String, Object> e : input.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Number) {
                ctx.put(e.getKey(), ((Number) v).doubleValue());
            } else if (v instanceof String) {
                String s = ((String) v).trim();
                if (s.isEmpty()) {
                    continue; // 空串可选参数（如下固定/加杆未填）不入上下文，按未传处理
                }
                if (isNumeric(s)) {
                    ctx.put(e.getKey(), Double.parseDouble(s));
                } else {
                    ctx.put(e.getKey(), s);
                }
            } else {
                ctx.put(e.getKey(), v);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();

        // 0) 默认值注入：vars 中带 list 的变量（如 单价 list:120），未传参时取 list 首个值作为默认
        JsonNode varsNode = cfg.get("vars");
        if (varsNode != null && varsNode.isArray()) {
            for (JsonNode vn : varsNode) {
                String vname = vn.path("name").asText("");
                if (vname.isEmpty() || ctx.containsKey(vname)) continue;
                String listStr = vn.path("list").asText("");
                if (listStr.isEmpty()) continue;
                String first = listStr.split("[,\uff0c]")[0].trim();
                if (isNumeric(first)) ctx.put(vname, Double.parseDouble(first));
            }
        }

        // 1) 变量组：除固定分组外的顶层数组节点（防护杆数量/孔位…）
        List<String> varNames = new ArrayList<>();
        Iterator<String> it = cfg.fieldNames();
        while (it.hasNext()) {
            String name = it.next();
            if (!FIXED_GROUPS.contains(name)) varNames.add(name);
        }
        for (String name : varNames) {
            Row row = firstMatchRow(cfg.get(name), ctx);
            if (row == null || row.expr == null) continue;
            Double v = FormulaExprEvaluator.evalNum(row.expr, ctx);
            ctx.put(name, v);
            out.put(name, round2(v));
        }

        // 2) 型材：支持行间互相引用（如 横杆=内框宽），多轮循环计算直到不再有新结果；
        //    不依赖 JSON 字段顺序（MySQL JSON 操作可能重排键序）
        Map<String, Object> profiles = new LinkedHashMap<>();
        JsonNode pfNode = cfg.get("型材");
        if (pfNode != null && pfNode.isObject()) {
            List<String> pending = new ArrayList<>();
            Iterator<String> pn = pfNode.fieldNames();
            while (pn.hasNext()) pending.add(pn.next());
            boolean progress = true;
            while (progress && !pending.isEmpty()) {
                progress = false;
                Iterator<String> pit = pending.iterator();
                while (pit.hasNext()) {
                    String name = pit.next();
                    Row row = firstMatchRow(pfNode.get(name), ctx);
                    if (row == null) {
                        pit.remove();
                        continue;
                    }
                    try {
                        // 表达式或数量公式可能引用尚未算出的型材变量，求值失败则留到下一轮
                        double dim = FormulaExprEvaluator.evalNum(row.expr, ctx);
                        double mult = row.mult == null ? 1 : FormulaExprEvaluator.evalNum(row.mult, ctx);
                        ctx.put(name, dim);
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("dim", round2(dim));
                        m.put("mult", round2(mult));
                        m.put("total", round2(dim * mult));
                        profiles.put(name, m);
                        pit.remove();
                        progress = true;
                    } catch (IllegalArgumentException ignore) {
                        // 依赖未就绪，下一轮再算
                    }
                }
            }
            // 循环结束后仍有未算出的行：再尝试一次，让真实错误（如笔误/未定义变量）向上抛出，便于用户修正公式
            for (String name : pending) {
                Row row = firstMatchRow(pfNode.get(name), ctx);
                if (row != null && row.expr != null) {
                    FormulaExprEvaluator.evalNum(row.expr, ctx);
                }
            }
        }
        out.put("型材", profiles);
        // 暴露常用引用值
        ctx.put("外框高", dimOf(profiles, "外框高"));
        ctx.put("外框宽", dimOf(profiles, "外框宽"));
        ctx.put("内框高", dimOf(profiles, "内框高"));
        ctx.put("内框宽", dimOf(profiles, "内框宽"));
        ctx.put("内扇高", dimOf(profiles, "内扇高"));
        ctx.put("内扇宽", dimOf(profiles, "内扇宽"));

        // 3) 纱网
        Map<String, Object> nets = new LinkedHashMap<>();
        JsonNode netNode = cfg.get("纱网");
        if (netNode != null && netNode.isObject()) {
            Iterator<String> nn = netNode.fieldNames();
            while (nn.hasNext()) {
                String name = nn.next();
                Row row = firstMatchRow(netNode.get(name), ctx);
                if (row == null) continue;
                double w = FormulaExprEvaluator.evalNum(row.w, ctx);
                double h = FormulaExprEvaluator.evalNum(row.h, ctx);
                double mult = row.mult == null ? 1 : FormulaExprEvaluator.evalNum(row.mult, ctx);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("w", round2(w));
                m.put("h", round2(h));
                m.put("mult", round2(mult));
                m.put("area", round2(w * h * 0.000001 * mult));
                nets.put(name, m);
            }
        }
        out.put("纱网", nets);

        // 4) 面积
        JsonNode areaNode = cfg.get("面积");
        if (areaNode != null) {
            Row row = firstMatchRow(areaNode, ctx);
            if (row != null && row.expr != null) {
                Double area = FormulaExprEvaluator.evalNum(row.expr, ctx);
                ctx.put("面积", area);
                out.put("面积", round2(area));
            }
        }

        // 5) 金额明细：同名多行满足条件全部累加
        Map<String, Object> amounts = new LinkedHashMap<>();
        double totalAmount = 0;
        JsonNode amtNode = cfg.get("金额明细");
        if (amtNode != null && amtNode.isObject()) {
            Iterator<String> an = amtNode.fieldNames();
            while (an.hasNext()) {
                String name = an.next();
                double acc = 0;
                boolean any = false;
                for (Row row : toRows(amtNode.get(name))) {
                    if (!matchCond(row.cond, ctx)) continue;
                    acc += FormulaExprEvaluator.evalNum(row.expr, ctx);
                    any = true;
                }
                if (any) {
                    double r = round2(acc);
                    amounts.put(name, r);
                    totalAmount += r;
                }
            }
        }
        out.put("金额明细", amounts);
        out.put("总金额", round2(totalAmount));
        return out;
    }

    // ==================== 工具 ====================

    private static Double dimOf(Map<String, Object> profiles, String name) {
        Object o = profiles.get(name);
        if (o instanceof Map) {
            Object d = ((Map<?, ?>) o).get("dim");
            if (d instanceof Number) return ((Number) d).doubleValue();
        }
        return null;
    }

    /** 取第一个满足条件的行；单对象包装成单行 */
    private static Row firstMatchRow(JsonNode node, Map<String, Object> ctx) {
        for (Row row : toRows(node)) {
            if (matchCond(row.cond, ctx)) return row;
        }
        return null;
    }

    private static List<Row> toRows(JsonNode node) {
        List<Row> rows = new ArrayList<>();
        if (node == null) return rows;
        if (node.isArray()) {
            for (JsonNode item : node) rows.add(parseRow(item));
        } else {
            rows.add(parseRow(node));
        }
        return rows;
    }

    private static Row parseRow(JsonNode n) {
        Row row = new Row();
        if (n.hasNonNull("cond")) row.cond = n.get("cond").asText();
        if (n.hasNonNull("expr")) row.expr = n.get("expr").asText();
        if (n.hasNonNull("mult")) row.mult = n.get("mult").asText();
        if (n.hasNonNull("w")) row.w = n.get("w").asText();
        if (n.hasNonNull("h")) row.h = n.get("h").asText();
        return row;
    }

    /** 条件匹配：null 视为无条件；{a,b} 花括号多条件 AND */
    private static boolean matchCond(String cond, Map<String, Object> ctx) {
        if (cond == null || cond.isEmpty()) return true;
        String c = cond.trim();
        if (c.startsWith("{") && c.endsWith("}")) {
            String inner = c.substring(1, c.length() - 1);
            for (String sub : inner.split(",")) {
                if (!FormulaExprEvaluator.evalBool(sub.trim(), ctx)) return false;
            }
            return true;
        }
        return FormulaExprEvaluator.evalBool(c, ctx);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
