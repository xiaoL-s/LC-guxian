package com.guxian.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户只读查询（共享 guxian-erp 库，直接查 sys_user）
 */
@Mapper
public interface SysUserReadMapper {

    @Select("SELECT real_name AS realName FROM sys_user WHERE id = #{userId} LIMIT 1")
    String queryRealName(Long userId);
}
