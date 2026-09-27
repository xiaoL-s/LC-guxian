package com.guxian.sales.enums;

import lombok.Getter;

/**
 * 销售订单状态机（主表 order_status 与明细行 item_status 共用前 8 个码值）
 * 0待受理 -> 1已受理 -> 2已开工(拆单生成工单,可打印下料单) -> 3打包入库 -> 4已发货 -> 5已完成
 * 0待受理 审核驳回 -> 6已驳回（修改后重新提交回到 0）
 * 待受理/已驳回 可取消 -> 7已取消（明细行取消为 9）
 *
 * 生产侧工序：开料 ∥ 剪网（并行） -> 组装 -> 打包 -> 入库；完工入库回写订单 3。
 */
@Getter
public enum OrderStatusEnum {

    PENDING_AUDIT(0, "待受理"),
    AUDITED(1, "已受理"),
    PRODUCING(2, "已开工"),
    FINISHED(3, "打包入库"),
    DELIVERED(4, "已发货"),
    COMPLETED(5, "已完成"),
    REJECTED(6, "已驳回"),
    CANCELED(7, "已取消"),

    /** 明细行专用：随订单取消 */
    ITEM_CANCELED(9, "已取消");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (OrderStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        return "";
    }

    /** 前端标签颜色（Element Plus el-tag type） */
    public static String tagOf(Integer code) {
        if (code == null) {
            return "info";
        }
        return switch (code) {
            case 0 -> "warning";
            case 1 -> "primary";
            case 2 -> "primary";
            case 3, 4 -> "success";
            case 5 -> "info";
            case 6 -> "danger";
            case 7, 9 -> "info";
            default -> "info";
        };
    }
}
