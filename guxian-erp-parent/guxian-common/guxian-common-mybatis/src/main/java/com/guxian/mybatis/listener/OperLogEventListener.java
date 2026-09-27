package com.guxian.mybatis.listener;

import com.guxian.context.UserContext;
import com.guxian.event.OperLogEvent;
import com.guxian.model.LoginUser;
import com.guxian.mybatis.entity.SysOperLog;
import com.guxian.mybatis.mapper.SysOperLogMapper;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器（放在 guxian-common-mybatis 公共模块，全局生效）
 * <p>
 * 所有引入 guxian-common-mybatis 的微服务（system / customer / 销售订单 / 供应商等）
 * 自动具备操作日志落库能力：Controller 方法加 @OperLog 注解即可，无需每个模块重复编写监听器。
 * <p>
 * 设计说明：
 * 1. 监听器直接注入公共模块的 SysOperLogMapper 落库，不依赖任何业务模块的 Service，
 *    避免 common → 业务模块的循环依赖；
 * 2. 事件发布与监听在同一线程同步执行，UserContext（ThreadLocal）有效，
 *    可直接拿到当前登录用户写入日志；
 * 3. mybatis 公共模块已依赖 security（提供 UserContext/LoginUser）与 core（提供事件），依赖链路无环。
 */
@Component
public class OperLogEventListener {

    @Resource
    private SysOperLogMapper sysOperLogMapper;

    @EventListener(OperLogEvent.class)
    public void listenOperLogEvent(OperLogEvent event) {
        // 事件转为数据库实体
        SysOperLog operLog = new SysOperLog();

        // 从 UserContext 获取登录用户（事件发布与监听同步执行，同一线程，UserContext 有效）
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null) {
            operLog.setUsername(loginUser.getUsername());
            operLog.setRealName(loginUser.getRealName());
        }

        operLog.setOperModule(event.getOperModule());
        operLog.setOperType(event.getOperType());
        operLog.setOperContent(event.getOperContent());
        operLog.setIp(event.getIp());
        operLog.setOperTime(event.getOperTime());
        // 直接通过公共 Mapper 落库
        sysOperLogMapper.insert(operLog);
    }
}
