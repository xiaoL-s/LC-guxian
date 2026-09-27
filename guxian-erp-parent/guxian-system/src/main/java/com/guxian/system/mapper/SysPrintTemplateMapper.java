package com.guxian.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.system.entity.SysPrintTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPrintTemplateMapper extends BaseMapper<SysPrintTemplate> {

    /**
     * 查询所有去重后的单据类型（用于下拉渲染）
     */
    @Select("SELECT DISTINCT template_type FROM t_print_template " +
            "WHERE del_flag = 0 AND template_type IS NOT NULL AND template_type <> '' " +
            "ORDER BY template_type")
    List<String> listDistinctTypes();
}
