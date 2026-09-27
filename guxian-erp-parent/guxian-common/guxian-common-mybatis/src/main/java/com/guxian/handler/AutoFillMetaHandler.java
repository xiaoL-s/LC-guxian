package com.guxian.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.guxian.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 公共字段自动填充
 * createBy、createTime、updateBy、updateTime 自动赋值，业务无需手动set
 */
@Component
public class AutoFillMetaHandler implements MetaObjectHandler {

    /**
     * 新增执行填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long loginUserId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // createBy 是Long类型，直接传值 + 指定类型
        this.strictInsertFill(metaObject, "createBy", Long.class, loginUserId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateBy", Long.class, loginUserId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    /**
     * 更新执行填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long loginUserId = UserContext.getUserId();
        this.strictUpdateFill(metaObject, "updateBy", Long.class, loginUserId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
}