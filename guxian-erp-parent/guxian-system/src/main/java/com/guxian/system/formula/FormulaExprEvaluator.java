package com.guxian.system.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纱窗产品公式表达式求值器（安全实现，不使用脚本引擎）
 *
 * <p>支持：数字、+ - * /、括号、比较(== != &lt;= &gt;= &lt; &gt;)、中文变量、中文字符串字面量、函数调用(lwxs)。</p>
 * <p>口径与前端公式引擎原型一致：<code>=</code> 视为相等比较；变量按名称长度降序替换；
 * 剩余裸中文按字符串字面量处理。</p>
 */
public final class FormulaExprEvaluator {

    private static final Pattern CN = Pattern.compile("[\\u4e00-\\u9fa5]+");
    private static final Pattern QUOTED = Pattern.compile("\"[^\"]*\"");

    private FormulaExprEvaluator() {
    }

    /**
     * 求值表达式
     *
     * @param expr 表达式，如 "总高-40" / "下固定=0" / "加杆=全防护"
     * @param ctx  变量上下文
     * @return Double / String / Boolean
     */
    public static Object eval(String expr, Map<String, Object> ctx) {
        String normalized = normalize(expr, ctx);
        return new Parser(tokenize(normalized), normalized).parse();
    }

    /** 数值求值，非数值抛异常 */
    public static double evalNum(String expr, Map<String, Object> ctx) {
        Object v = eval(expr, ctx);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        throw new IllegalArgumentException("表达式不是数值: " + expr);
    }

    /** 布尔条件求值 */
    public static boolean evalBool(String expr, Map<String, Object> ctx) {
        Object v = eval(expr, ctx);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue() != 0;
        }
        throw new IllegalArgumentException("表达式不是布尔条件: " + expr);
    }

    // ==================== 预处理 ====================

    static String normalize(String expr, Map<String, Object> ctx) {
        String s = expr;
        // 1) 公式里的 "=" 是相等比较 -> "=="（不影响 <= >=）
        s = s.replaceAll("(?<![=<>!])=(?!=)", "==");
        // 2) 变量替换：按长度降序 + 中文/字母/数字/下划线边界，防子串误替换（如"密码把手"中的"把手"）
        List<String> keys = new ArrayList<>(ctx.keySet());
        keys.sort((a, b) -> b.length() - a.length());
        for (String k : keys) {
            Object v = ctx.get(k);
            String rep;
            if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                rep = d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            } else {
                rep = "\"" + String.valueOf(v).replace("\"", "") + "\"";
            }
            String pattern = "(?<![\\u4e00-\\u9fa5A-Za-z0-9_])" + Pattern.quote(k) + "(?![\\u4e00-\\u9fa5A-Za-z0-9_])";
            s = s.replaceAll(pattern, Matcher.quoteReplacement(rep));
        }
        // 3) 剩余裸中文 -> 字符串字面量（先保护已带引号的串，避免重复包裹）
        List<String> quoted = new ArrayList<>();
        Matcher qm = QUOTED.matcher(s);
        StringBuffer sb1 = new StringBuffer();
        while (qm.find()) {
            quoted.add(qm.group());
            qm.appendReplacement(sb1, Matcher.quoteReplacement("\u0000" + (quoted.size() - 1) + "\u0000"));
        }
        qm.appendTail(sb1);
        String s2 = sb1.toString();

        Matcher cm = CN.matcher(s2);
        StringBuffer sb2 = new StringBuffer();
        while (cm.find()) {
            cm.appendReplacement(sb2, Matcher.quoteReplacement("\"" + cm.group() + "\""));
        }
        cm.appendTail(sb2);
        String s3 = sb2.toString();

        for (int i = 0; i < quoted.size(); i++) {
            s3 = s3.replace("\u0000" + i + "\u0000", quoted.get(i));
        }
        return s3;
    }

    // ==================== Token ====================

    private enum TokType {NUM, STR, IDENT, OP, CMP, LP, RP, COMMA, EOF}

    private static final class Tok {
        TokType type;
        String text;

        Tok(TokType type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    static List<Tok> tokenize(String s) {
        List<Tok> toks = new ArrayList<>();
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c >= '0' && c <= '9') {
                int j = i;
                while (j < n && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) j++;
                toks.add(new Tok(TokType.NUM, s.substring(i, j)));
                i = j;
            } else if (c == '"') {
                int j = s.indexOf('"', i + 1);
                if (j < 0) throw new IllegalArgumentException("字符串未闭合: " + s);
                toks.add(new Tok(TokType.STR, s.substring(i + 1, j)));
                i = j + 1;
            } else if (isIdentChar(c)) {
                int j = i;
                while (j < n && isIdentChar(s.charAt(j))) j++;
                toks.add(new Tok(TokType.IDENT, s.substring(i, j)));
                i = j;
            } else if (c == '(') {
                toks.add(new Tok(TokType.LP, "("));
                i++;
            } else if (c == ')') {
                toks.add(new Tok(TokType.RP, ")"));
                i++;
            } else if (c == ',') {
                toks.add(new Tok(TokType.COMMA, ","));
                i++;
            } else if (c == '=' || c == '!' || c == '<' || c == '>') {
                String two = i + 1 < n ? s.substring(i, i + 2) : "";
                if (two.equals("==") || two.equals("!=") || two.equals("<=") || two.equals(">=")) {
                    toks.add(new Tok(TokType.CMP, two));
                    i += 2;
                } else if (c == '<' || c == '>') {
                    toks.add(new Tok(TokType.CMP, String.valueOf(c)));
                    i++;
                } else {
                    throw new IllegalArgumentException("非法运算符: " + c + " in " + s);
                }
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                toks.add(new Tok(TokType.OP, String.valueOf(c)));
                i++;
            } else {
                throw new IllegalArgumentException("非法字符: " + c + " in " + s);
            }
        }
        toks.add(new Tok(TokType.EOF, ""));
        return toks;
    }

    private static boolean isIdentChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '_' || (c >= '\u4e00' && c <= '\u9fa5');
    }

    // ==================== 递归下降解析 ====================

    private static final class Parser {
        private final List<Tok> toks;
        private final String src;
        private int pos;

        Parser(List<Tok> toks, String src) {
            this.toks = toks;
            this.src = src;
        }

        Object parse() {
            Object v = comparison();
            if (peek().type != TokType.EOF) {
                throw new IllegalArgumentException("表达式有多余内容: " + src);
            }
            return v;
        }

        private Object comparison() {
            Object left = additive();
            while (peek().type == TokType.CMP) {
                String op = next().text;
                Object right = additive();
                left = applyCmp(op, left, right);
            }
            return left;
        }

        private Object additive() {
            Object left = multiplicative();
            while (peek().type == TokType.OP && (peek().text.equals("+") || peek().text.equals("-"))) {
                String op = next().text;
                Object right = multiplicative();
                left = applyArith(op, left, right);
            }
            return left;
        }

        private Object multiplicative() {
            Object left = unary();
            while (peek().type == TokType.OP && (peek().text.equals("*") || peek().text.equals("/"))) {
                String op = next().text;
                Object right = unary();
                left = applyArith(op, left, right);
            }
            return left;
        }

        private Object unary() {
            if (peek().type == TokType.OP && peek().text.equals("-")) {
                next();
                Object v = unary();
                if (v instanceof Number) {
                    return -((Number) v).doubleValue();
                }
                throw new IllegalArgumentException("一元负号只支持数字: " + src);
            }
            return primary();
        }

        private Object primary() {
            Tok t = next();
            switch (t.type) {
                case NUM:
                    return Double.parseDouble(t.text);
                case STR:
                    return t.text;
                case LP: {
                    Object v = comparison();
                    expect(TokType.RP, ")");
                    return v;
                }
                case IDENT: {
                    // 函数调用（变量已在预处理阶段全部替换为字面量）
                    if (peek().type == TokType.LP) {
                        next();
                        List<Object> args = new ArrayList<>();
                        if (peek().type != TokType.RP) {
                            args.add(comparison());
                            while (peek().type == TokType.COMMA) {
                                next();
                                args.add(comparison());
                            }
                        }
                        expect(TokType.RP, ")");
                        return callFunction(t.text, args);
                    }
                    throw new IllegalArgumentException("未定义变量: " + t.text + " in " + src);
                }
                default:
                    throw new IllegalArgumentException("意外的 token: " + t.text + " in " + src);
            }
        }

        private Object callFunction(String name, List<Object> args) {
            if ("lwxs".equals(name)) { // 占位：恒等系数函数，后续可按真实口径替换
                return args.isEmpty() ? 0d : args.get(0);
            }
            throw new IllegalArgumentException("未定义函数: " + name);
        }

        private Object applyArith(String op, Object a, Object b) {
            double x = asNum(a, op);
            double y = asNum(b, op);
            switch (op) {
                case "+":
                    return x + y;
                case "-":
                    return x - y;
                case "*":
                    return x * y;
                case "/":
                    if (y == 0) throw new IllegalArgumentException("除数为0: " + src);
                    return x / y;
                default:
                    throw new IllegalArgumentException("非法运算符: " + op);
            }
        }

        private Object applyCmp(String op, Object a, Object b) {
            // 宽容处理：一方数字一方字符串时，字符串按 0 参与数值比较；
            // （公式条件中引用了未传入的可选变量，如 下固定>0，未填视为 0，取"等于0"分支）
            if ((a instanceof Number) != (b instanceof Number)) {
                double x = numOfOrZero(a);
                double y = numOfOrZero(b);
                switch (op) {
                    case "==": return x == y;
                    case "!=": return x != y;
                    case "<":  return x < y;
                    case "<=": return x <= y;
                    case ">":  return x > y;
                    case ">=": return x >= y;
                    default: throw new IllegalArgumentException("非法比较符: " + op);
                }
            }
            if (a instanceof Number && b instanceof Number) {
                double x = ((Number) a).doubleValue();
                double y = ((Number) b).doubleValue();
                switch (op) {
                    case "==": return x == y;
                    case "!=": return x != y;
                    case "<":  return x < y;
                    case "<=": return x <= y;
                    case ">":  return x > y;
                    case ">=": return x >= y;
                    default: throw new IllegalArgumentException("非法比较符: " + op);
                }
            }
            String sx = a == null ? "" : a.toString();
            String sy = b == null ? "" : b.toString();
            switch (op) {
                case "==": return sx.equals(sy);
                case "!=": return !sx.equals(sy);
                default: throw new IllegalArgumentException("字符串只支持 == / != 比较: " + op);
            }
        }

        private double asNum(Object v, String op) {
            if (v instanceof Number) return ((Number) v).doubleValue();
            if (v instanceof String) {
                try {
                    return Double.parseDouble(((String) v).trim());
                } catch (NumberFormatException e) {
                    // 未传入的可选变量在算术中按 0 处理（如 单价*面积 中单价缺省）
                    return 0d;
                }
            }
            throw new IllegalArgumentException("运算符 " + op + " 需要数值，得到: " + v + " in " + src);
        }

        /** 数字或可转数字的字符串返回数字；否则抛异常 */
        private static double numOf(Object v) {
            if (v instanceof Number) return ((Number) v).doubleValue();
            if (v instanceof String) {
                try {
                    return Double.parseDouble(((String) v).trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("非数字: " + v);
                }
            }
            throw new IllegalArgumentException("非数字: " + v);
        }

        /** 数字或可转数字的字符串返回数字；转不了的字符串按 0 处理（未定义变量语义） */
        private static double numOfOrZero(Object v) {
            try {
                return numOf(v);
            } catch (IllegalArgumentException e) {
                return 0d;
            }
        }

        private Tok peek() {
            return toks.get(pos);
        }

        private Tok next() {
            return toks.get(pos++);
        }

        private void expect(TokType type, String text) {
            Tok t = next();
            if (t.type != type) {
                throw new IllegalArgumentException("期望 " + text + "，得到: " + t.text + " in " + src);
            }
        }
    }
}
