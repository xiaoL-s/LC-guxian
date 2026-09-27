package com.guxian.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.entity.SysDictType;

public interface SysDictTypeService extends IService<SysDictType> {
    IPage<SysDictType> page(Page<SysDictType> page, LambdaQueryWrapper<SysDictType> wrapper);
}
