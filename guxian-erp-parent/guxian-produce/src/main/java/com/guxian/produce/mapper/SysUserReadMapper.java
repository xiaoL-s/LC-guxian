package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 只读用户信息（报工选工人/操作人）
 */
@Mapper
public interface SysUserReadMapper {

    @Select("SELECT id, username, real_name AS realName FROM sys_user WHERE status = 1 AND del_flag = 0 ORDER BY id")
    List<Map<String, Object>> selectEnabledUsers();

    @Select("SELECT id, username, real_name AS realName FROM sys_user WHERE id = #{userId}")
    Map<String, Object> selectUser(@Param("userId") Long userId);
}
