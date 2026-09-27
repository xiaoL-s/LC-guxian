package com.guxian.sales.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 订单财务（收账）状态
 * 0未收账 -> 1部分已收账 -> 2已收账 -> 3已结清
 * 结清为人工确认动作（如尾差抹零），其余由收款登记自动推导。
 */
@Getter
public enum FinanceStatusEnum {

    UNPAID(0, "未收账"),
    PART_PAID(1, "部分已收账"),
    PAID(2, "已收账"),
    SETTLED(3, "已结清");

    private final Integer code;
    private final String desc;

    FinanceStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (FinanceStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        return "";
    }

    /** 前端标签颜色 */
    public static String tagOf(Integer code) {
        if (code == null) {
            return "info";
        }
        return switch (code) {
            case 0 -> "danger";
            case 1 -> "warning";
            case 2 -> "success";
            case 3 -> "info";
            default -> "info";
        };
    }

    /**
     * 根据订单总金额与累计实收推导财务状态
     */
    public static Integer derive(BigDecimal totalAmount, BigDecimal receiveAmount) {
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        BigDecimal received = receiveAmount == null ? BigDecimal.ZERO : receiveAmount;
        if (received.signum() <= 0) {
            return UNPAID.getCode();
        }
        if (received.compareTo(total) >= 0) {
            return PAID.getCode();
        }
        return PART_PAID.getCode();
    }
}
