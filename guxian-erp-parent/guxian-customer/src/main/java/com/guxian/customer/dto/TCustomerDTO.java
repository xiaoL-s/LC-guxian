package com.guxian.customer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TCustomerDTO {
    private Long customerId;
    private String customerName;
    private String contact;
    private String phone;
    private String address;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
