package com.guxian.mini.controller;

import com.guxian.mini.util.UserContext;
import com.guxian.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 工人端：我的任务、扫码报工、我的工资
 * 所有业务通过HTTP转发到 produce(8085)，不重复写业务
 */
@RestController
@RequestMapping("/mini/work")
public class WorkController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${guxian.produce.base-url}")
    private String produceBase;

    private HttpEntity<Void> forward(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        return new HttpEntity<>(h);
    }

    /** 我的工单任务列表（待报工/生产中） */
    @GetMapping("/tasks")
    @SuppressWarnings("unchecked")
    public Result<Object> tasks(@RequestHeader("Authorization") String token,
                                @RequestParam(required = false) String workStatus) {
        String url = produceBase + "/production/workorder/page?pageNum=1&pageSize=50";
        if (workStatus != null && !workStatus.isEmpty()) url += "&workStatus=" + workStatus;
        ResponseEntity<Map> r = restTemplate.exchange(url, HttpMethod.GET, forward(token), Map.class);
        Map data = (Map) r.getBody().get("data");
        if (data != null && data.get("records") != null) {
            java.util.List<Map<String, Object>> records = (java.util.List<Map<String, Object>>) data.get("records");
            // 逐个补客户名和状态文案（列表接口不带这些字段）
            for (Map<String, Object> row : records) {
                try {
                    Long wid = Long.valueOf(row.get("workId").toString());
                    ResponseEntity<Map> dr = restTemplate.exchange(produceBase + "/production/workorder/" + wid,
                            HttpMethod.GET, forward(token), Map.class);
                    Map detail = (Map) dr.getBody().get("data");
                    if (detail != null) {
                        row.put("customerName", detail.get("customerName"));
                        row.put("workStatusDesc", detail.get("workStatusDesc"));
                        row.put("currentStage", detail.get("workStatusDesc"));
                    }
                } catch (Exception ignored) {}
            }
        }
        return Result.success(data);
    }

    /** 工单详情（含工序进度） */
    @GetMapping("/detail/{workId}")
    public Result<Object> detail(@RequestHeader("Authorization") String token, @PathVariable Long workId) {
        ResponseEntity<Map> r = restTemplate.exchange(produceBase + "/production/workorder/" + workId,
                HttpMethod.GET, forward(token), Map.class);
        Map body = r.getBody();
        return Result.success(body == null ? null : body.get("data"));
    }

    /** 所有启用工序（扫码报工选择） */
    @GetMapping("/processes")
    public Result<Object> processes(@RequestHeader("Authorization") String token) {
        ResponseEntity<Map> r = restTemplate.exchange(produceBase + "/production/process/listEnabled",
                HttpMethod.GET, forward(token), Map.class);
        Map body = r.getBody();
        return Result.success(body == null ? null : body.get("data"));
    }

    /** 扫码报工 */
    @PostMapping("/report")
    public Result<Object> report(@RequestHeader("Authorization") String token,
                                 @RequestBody Map<String, Object> body) {
        // 工人id强制用当前登录人，防止前端伪造
        body.put("workerId", UserContext.getUserId());
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", token);
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> e = new HttpEntity<>(body, h);
        ResponseEntity<Map> r = restTemplate.postForEntity(produceBase + "/production/workorder/report", e, Map.class);
        Map body2 = r.getBody();
        // produce 返回 Result 包装：code!=200 时透传错误信息，避免前端误报成功
        Object code = body2 == null ? null : body2.get("code");
        if (code != null && !"200".equals(String.valueOf(code))) {
            return Result.fail(String.valueOf(body2.get("msg")));
        }
        return Result.success((String) body2.get("msg"), null);
    }

    /**
     * 解析二维码 → 工单报工信息
     * 新格式：GX1|WO|{workId}|{workNo}|{yyyyMMdd}
     * 兼容旧格式：GX-WORK:{workId}:{workNo}、纯工单号
     */
    @PostMapping("/qrParse")
    public Result<Object> qrParse(@RequestHeader("Authorization") String token,
                                  @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("二维码内容为空");
        }
        String raw = content.trim();
        Long workId = null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("GX1\\|WO\\|(\\d+)").matcher(raw);
        if (m.find()) {
            workId = Long.valueOf(m.group(1));
        } else {
            m = java.util.regex.Pattern.compile("GX-WORK[:\\-](\\d+)").matcher(raw);
            if (m.find()) {
                workId = Long.valueOf(m.group(1));
            } else {
                m = java.util.regex.Pattern.compile("(\\d+)").matcher(raw);
                if (m.find()) {
                    workId = Long.valueOf(m.group(1));
                }
            }
        }
        if (workId == null) {
            return Result.fail("二维码无法识别");
        }
        ResponseEntity<Map> r = restTemplate.exchange(produceBase + "/production/workorder/scanInfo?workId=" + workId,
                HttpMethod.GET, forward(token), Map.class);
        Map body2 = r.getBody();
        Map data = body2 == null ? null : (Map) body2.get("data");
        if (data == null || data.get("workNo") == null) {
            return Result.fail("未找到该工单");
        }
        return Result.success(data);
    }

    /** 我的收入：今日/昨日/本月/上月 数量与工资统计 */
    @GetMapping("/income")
    public Result<Object> income(@RequestHeader("Authorization") String token) {
        Long uid = UserContext.getUserId();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);
        java.time.LocalDate monthStart = today.withDayOfMonth(1);
        java.time.LocalDate lastMonthStart = monthStart.minusMonths(1);
        java.time.LocalDate lastMonthEnd = monthStart.minusDays(1);

        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        addIncome(list, token, uid, "今日", today.toString(), today.toString());
        addIncome(list, token, uid, "昨日", yesterday.toString(), yesterday.toString());
        addIncome(list, token, uid, "本月", monthStart.toString(), today.toString());
        addIncome(list, token, uid, "上月", lastMonthStart.toString(), lastMonthEnd.toString());

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("realName", UserContext.getRealName());
        data.put("postType", UserContext.getPostType());
        data.put("userId", uid);
        data.put("items", list);
        return Result.success(data);
    }

    @SuppressWarnings("unchecked")
    private void addIncome(java.util.List<java.util.Map<String, Object>> list, String token,
                           Long uid, String label, String dateFrom, String dateTo) {
        try {
            String url = produceBase + "/production/report/wage/summary?workerId=" + uid
                    + "&dateFrom=" + dateFrom + "&dateTo=" + dateTo;
            ResponseEntity<Map> r = restTemplate.exchange(url, HttpMethod.GET, forward(token), Map.class);
            Map body = r.getBody();
            Map data = body == null ? null : (Map) body.get("data");
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("label", label);
            row.put("dateFrom", dateFrom);
            row.put("dateTo", dateTo);
            if (data != null) {
                row.put("qualifiedNum", data.get("totalQualified"));
                row.put("badNum", data.get("totalBad"));
                row.put("pieceWage", data.get("totalWage"));
                row.put("count", data.get("count"));
            } else {
                row.put("qualifiedNum", 0);
                row.put("badNum", 0);
                row.put("pieceWage", 0);
                row.put("count", 0);
            }
            list.add(row);
        } catch (Exception e) {
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("label", label);
            row.put("dateFrom", dateFrom);
            row.put("dateTo", dateTo);
            row.put("qualifiedNum", 0);
            row.put("badNum", 0);
            row.put("pieceWage", 0);
            row.put("count", 0);
            list.add(row);
        }
    }

    /** 我的计件工资（按月） */
    @GetMapping("/wage")
    public Result<Object> wage(@RequestHeader("Authorization") String token,
                               @RequestParam(required = false) String month) {
        Long uid = UserContext.getUserId();
        String url = produceBase + "/production/report/wage/byWorker?workerId=" + uid;
        if (month != null && !month.isEmpty()) url += "&month=" + month;
        ResponseEntity<Map> r = restTemplate.exchange(url, HttpMethod.GET, forward(token), Map.class);
        Map body = r.getBody();
        return Result.success(body == null ? null : body.get("data"));
    }
}
