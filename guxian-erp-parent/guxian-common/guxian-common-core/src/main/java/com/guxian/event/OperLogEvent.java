package com.guxian.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperLogEvent {
    /**
     * 账号
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 操作模块
     */
    private String operModule;
    /**
     * 操作类型
     */
    private String operType;
    /**
     * 操作描述
     */
    private String operContent;
    /**
     * IP地址
     */
    private String ip;
    /**
     * 操作时间
     */
    private LocalDateTime operTime;
}
