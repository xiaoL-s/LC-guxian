package com.guxian.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    /**
     * 查询已被逻辑删除的字典类型（忽略 del_flag），用于字典类型编码复用时避免唯一索引冲突
     */
    @Select("SELECT * FROM sys_dict_type WHERE dict_type = #{dictType} AND del_flag = 1 LIMIT 1")
    SysDictType selectDeletedByDictType(@Param("dictType") String dictType);

    /**
     * 恢复逻辑删除标记
     */
    @Update("UPDATE sys_dict_type SET del_flag = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
