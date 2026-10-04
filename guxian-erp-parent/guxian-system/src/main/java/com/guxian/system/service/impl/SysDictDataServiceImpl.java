package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.result.Result;
import com.guxian.system.dto.FormulaTestRequest;
import com.guxian.system.entity.SysDictData;
import com.guxian.system.formula.ProductFormulaCalculator;
import com.guxian.system.mapper.SysDictDataMapper;
import com.guxian.system.service.SysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public IPage<SysDictData> page(Page<SysDictData> page, LambdaQueryWrapper<SysDictData> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Result<Map<String, Object>> formulaTest(FormulaTestRequest request) {
        if (request == null || !StringUtils.hasText(request.getDictValue())) {
            return Result.fail("缺少产品字典值");
        }
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(request.getDictType()), SysDictData::getDictType, request.getDictType())
                .eq(SysDictData::getDictValue, request.getDictValue())
                .last("LIMIT 1");
        SysDictData data = getOne(wrapper);
        if (data == null) {
            return Result.fail("未找到该产品字典项");
        }
        if (!StringUtils.hasText(data.getFormulaConfig())) {
            return Result.fail("该产品未配置公式（请在字典项中填写 formulaConfig）");
        }
        try {
            Map<String, Object> result = ProductFormulaCalculator.calc(data.getFormulaConfig(),
                    request.getParams() == null ? Map.of() : request.getParams());
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("公式计算失败: " + e.getMessage());
        }
    }
}
