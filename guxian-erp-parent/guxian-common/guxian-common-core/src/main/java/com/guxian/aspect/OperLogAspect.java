package com.guxian.aspect;

import com.guxian.annotation.OperLog;
import com.guxian.event.OperLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面（放在 common-core 公共模块）
 * 只负责采集操作信息并发布事件，不依赖 security 模块，避免循环依赖
 * 用户信息由 system 模块的监听器从 UserContext 获取后入库
 */
@Aspect
@Component
public class OperLogAspect {

    // 注入Spring事件发布器，不是业务service！！
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Pointcut("@annotation(com.guxian.annotation.OperLog)")
    public void operLogPointCut() {
    }

    @AfterReturning("operLogPointCut()")
    public void saveOperLog(JoinPoint joinPoint) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperLog operLogAnno = signature.getMethod().getAnnotation(OperLog.class);
        if (operLogAnno == null) {
            return;
        }

        // 组装事件对象（用户信息由监听器从UserContext获取，这里不取，避免core依赖security）
        OperLogEvent event = new OperLogEvent();
        event.setOperModule(operLogAnno.operModule());
        event.setOperType(operLogAnno.operType());
        event.setOperContent(operLogAnno.operContent());
        event.setIp(getIpAddress(request));
        event.setOperTime(LocalDateTime.now());

        // 发布事件，交给system模块监听入库
        applicationEventPublisher.publishEvent(event);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
