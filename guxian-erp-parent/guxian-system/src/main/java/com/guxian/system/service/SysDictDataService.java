package com.guxian.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.entity.SysDictData;


public interface SysDictDataService extends IService<SysDictData> {
    IPage<SysDictData> page(Page<SysDictData> page, LambdaQueryWrapper<SysDictData> wrapper);
}
