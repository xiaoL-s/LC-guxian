package com.guxian.customer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TCustomerFollowDTO {
    private Long id;
    private Long customerId;
    private String followContent;
    private LocalDateTime followTime;
    private String followUser;
    private LocalDateTime createTime;
}
