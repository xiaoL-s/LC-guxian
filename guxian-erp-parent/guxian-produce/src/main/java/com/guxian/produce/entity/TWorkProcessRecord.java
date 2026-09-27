package com.guxian.produce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单工序计件流水 t_work_process_record（同工单同工序唯一）
 */
@Data
@TableName("t_work_process_record")
public class TWorkProcessRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /** 工单ID */
    private Long workId;

    /** 工单号 */
    private String workNo;

    /** 工序ID */
    private Long processId;

    /** 工序编码 */
    private String processCode;

    /** 操作工人ID */
    private Long operUserId;

    /** 操作工人名称 */
    private String operUsername;

    /** 扫码计件时间 */
    private LocalDateTime scanTime;

    /** PASS合格 FAIL不合格 */
    private String resultStatus;

    private String remark;
}
