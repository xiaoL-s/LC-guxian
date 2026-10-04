package com.guxian.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.result.Result;
import com.guxian.system.dto.FormulaTestRequest;
import com.guxian.system.entity.SysDictData;

import java.util.Map;


public interface SysDictDataService extends IService<SysDictData> {
    IPage<SysDictData> page(Page<SysDictData> page, LambdaQueryWrapper<SysDictData> wrapper);

    /**
     * 产品公式测试：根据字典类型+字典值取公式配置，用输入参数计算下料/剪网/面积/金额
     */
    Result<Map<String, Object>> formulaTest(FormulaTestRequest request);
}
