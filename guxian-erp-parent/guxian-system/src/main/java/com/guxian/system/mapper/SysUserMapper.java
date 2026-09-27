package com.guxian.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询已被逻辑删除的用户（忽略 del_flag），用于账号复用时避免唯一索引冲突
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND del_flag = 1 LIMIT 1")
    SysUser selectDeletedByUsername(@Param("username") String username);

    /**
     * 恢复逻辑删除标记
     */
    @Update("UPDATE sys_user SET del_flag = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
