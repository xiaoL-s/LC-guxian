package com.guxian.sales.vo;

import lombok.Data;

/**
 * 订单明细（子单/产品行）分页查询条件
 */
@Data
public class OrderItemQueryVO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 订单号模糊 */
    private String orderNo;
    /** 子单号模糊 */
    private String subOrderNo;
    /** 客户名称模糊 */
    private String customerName;
    /** 产品名称模糊 */
    private String productName;
    /** 品目（字典系列） */
    private String itemCategory;
    /** 订单状态 */
    private Integer orderStatus;
    /** 行状态 */
    private Integer itemStatus;
    /** 财务状态 */
    private Integer financeStatus;
    /** 订单类型 */
    private String orderType;
    /** 业务员（订单头） */
    private String salesman;
    /** 销售商/下单员（明细行） */
    private String salesOwner;
    /** 下单开始日期 */
    private String dateFrom;
    /** 下单结束日期 */
    private String dateTo;
    /** 关键字（订单号/子单号/客户/产品 任一命中） */
    private String keyword;
}
