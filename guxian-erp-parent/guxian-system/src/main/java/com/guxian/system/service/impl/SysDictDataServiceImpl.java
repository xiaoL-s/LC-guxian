package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.entity.SysDictData;
import com.guxian.system.mapper.SysDictDataMapper;
import com.guxian.system.service.SysDictDataService;
import org.springframework.stereotype.Service;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public IPage<SysDictData> page(Page<SysDictData> page, LambdaQueryWrapper<SysDictData> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }
}
