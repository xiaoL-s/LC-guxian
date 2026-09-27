package com.guxian.produce.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 只读货架信息（同库共享 stock 模块）
 */
@Mapper
public interface TShelfReadMapper {

    @Select("SELECT shelf_id AS shelfId, shelf_name AS shelfName FROM t_shelf WHERE shelf_id = #{shelfId} AND del_flag = 0")
    Map<String, Object> selectShelf(@Param("shelfId") Long shelfId);

    /** 取第一个启用货架（销售侧一键开工未指定货架时自动分配） */
    @Select("SELECT shelf_id AS shelfId, shelf_name AS shelfName FROM t_shelf WHERE status = 1 AND del_flag = 0 ORDER BY sort ASC, shelf_id ASC LIMIT 1")
    Map<String, Object> selectFirstEnabled();
}
