package com.guxian.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysOperLogDTO {
    private Long id;
    private String username;
    private String realName;
    private String operModule;
    private String operType;
    private String operContent;
    private String ip;
    private LocalDateTime operTime;
}
