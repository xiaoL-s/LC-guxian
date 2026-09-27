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
        return Result.success((String) body2.get("msg"), null);
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
