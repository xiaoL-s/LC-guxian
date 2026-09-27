package com.guxian.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.mybatis.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper（放在 guxian-common-mybatis 公共模块，全局唯一）
 * 由公共模块 MyBatisPlusConfig 的 @MapperScan("com.guxian.*.mapper") 自动扫描注册
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
