package com.guxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guxian.entity.TWorkerWage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 月度工资确认表（t_worker_wage）
 */
@Mapper
public interface TWorkerWageMapper extends BaseMapper<TWorkerWage> {
}
