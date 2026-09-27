package com.guxian.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    /**
     * 操作模块
     */
    String operModule() default "";

    /**
     * 操作类型：新增 / 修改 / 删除 / 查询
     */
    String operType() default "";

    /**
     * 操作描述
     */
    String operContent() default "";
}
