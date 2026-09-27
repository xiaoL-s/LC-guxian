package com.guxian.sales.vo;

import lombok.Data;

/**
 * 销售订单（订单维度）分页查询条件
 */
@Data
public class SalesOrderQueryVO {
    /** 当前页 */
    private Integer pageNum = 1;
    /** 每页条数 */
    private Integer pageSize = 10;
    /** 订单号模糊 */
    private String orderNo;
    /** 子单号模糊 */
    private String subOrderNo;
    /** 客户名称模糊 */
    private String customerName;
    /** 订单状态 */
    private Integer orderStatus;
    /** 财务状态 */
    private Integer financeStatus;
    /** 订单类型 */
    private String orderType;
    /** 业务员 */
    private String salesman;
    /** 下单开始日期 */
    private String dateFrom;
    /** 下单结束日期 */
    private String dateTo;
}
