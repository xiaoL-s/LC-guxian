package com.guxian.constant;

import java.util.Map;

/**
 * 工单、工序、打印全局常量
 */
public class WorkConstant {

    // ===================== 工单状态 =====================
    /** 0：待开料 */
    public static final Integer STATUS_PENDING_CUT = 0;
    /** 1：开料完成 */
    public static final Integer STATUS_CUT_DONE = 1;
    /** 2：组装完成 */
    public static final Integer STATUS_ASSEMBLE_DONE = 2;
    /** 3：质检完成 */
    public static final Integer STATUS_INSPECT_DONE = 3;
    /** 4：打包完工（工单结束） */
    public static final Integer STATUS_PACK_DONE = 4;

    // ===================== 工人岗位 -> 工序编码映射 =====================
    public static final Map<String, String> POST_TO_PROCESS_MAP = Map.of(
            "cut", "1",
            "assemble", "2",
            "inspect", "3",
            "package", "4"
    );

    // ===================== 打印模板类型 =====================
    public static final String PRINT_TEMPLATE_WORK_ORDER = "WORK_ORDER";
}