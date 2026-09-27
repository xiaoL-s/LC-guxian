package com.guxian.exception;

import lombok.Getter;

/**
 * 全局业务错误码枚举，统一管理异常编码
 */
@Getter
public enum ResultCodeEnum {

    // 通用
    SUCCESS(200, "请求成功"),
    FAIL(500, "请求失败"),
    PARAM_ERROR(400, "参数校验失败"),
    TOKEN_EXPIRE(401, "登录已失效，请重新登录"),
    NO_PERMISSION(403, "暂无操作权限"),
    NOT_FOUND(404, "资源不存在"),

    // 工单业务 10000 段
    WORK_ORDER_NOT_EXIST(10001, "生产工单不存在"),
    PRE_PROCESS_UNFINISHED(10002, "前置工序尚未完成，禁止当前工序报工"),
    PROCESS_REPEAT_SUBMIT(10003, "该工序已完成报工，请勿重复提交"),
    POST_PROCESS_MISMATCH(10004, "当前岗位无法操作该工序"),

    // 销售订单 20000段
    SALES_ORDER_NOT_EXIST(20001, "销售订单不存在"),
    AREA_CALC_ERROR(20002, "面积计算异常"),

    // 客户模块30000段
    CUSTOMER_NOT_EXIST(30001, "客户信息不存在");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}