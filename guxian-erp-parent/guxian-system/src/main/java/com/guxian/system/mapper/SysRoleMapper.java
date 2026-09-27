package com.guxian.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询已被逻辑删除的角色（忽略 del_flag），用于角色编码复用时避免唯一索引冲突
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND del_flag = 1 LIMIT 1")
    SysRole selectDeletedByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 恢复逻辑删除标记
     */
    @Update("UPDATE sys_role SET del_flag = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
