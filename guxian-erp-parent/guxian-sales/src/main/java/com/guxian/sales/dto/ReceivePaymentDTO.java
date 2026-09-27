package com.guxian.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款登记
 */
@Data
public class ReceivePaymentDTO {
    /** 本次收款金额 */
    private BigDecimal amount;
    /** 备注 */
    private String remark;
    /** 收款时间（不填取当前时间） */
    private LocalDateTime receiveTime;
}
