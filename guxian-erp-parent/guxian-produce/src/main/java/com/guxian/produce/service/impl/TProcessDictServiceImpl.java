package com.guxian.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.exception.BusinessException;
import com.guxian.produce.entity.TProcessDict;
import com.guxian.produce.mapper.TProcessDictMapper;
import com.guxian.produce.service.TProcessDictService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TProcessDictServiceImpl extends ServiceImpl<TProcessDictMapper, TProcessDict> implements TProcessDictService {

    @Override
    public List<TProcessDict> listEnabled() {
        LambdaQueryWrapper<TProcessDict> w = new LambdaQueryWrapper<>();
        w.eq(TProcessDict::getStatus, 1).orderByAsc(TProcessDict::getProcessSort);
        return baseMapper.selectList(w);
    }

    @Override
    public void saveProcess(TProcessDict process) {
        if (!StringUtils.hasText(process.getProcessName())) {
            throw new BusinessException("工序名称不能为空");
        }
        if (!StringUtils.hasText(process.getProcessCode())) {
            throw new BusinessException("工序编码不能为空");
        }
        if (process.getUnitPrice() == null) {
            process.setUnitPrice(java.math.BigDecimal.ZERO);
        }
        if (process.getStatus() == null) {
            process.setStatus(1);
        }
        if (process.getProcessSort() == null) {
            process.setProcessSort(0);
        }
        if (process.getProcessId() == null) {
            baseMapper.insert(process);
        } else {
            baseMapper.updateById(process);
        }
    }
}
