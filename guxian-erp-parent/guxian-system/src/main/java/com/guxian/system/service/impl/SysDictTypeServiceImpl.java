package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.entity.SysDictType;
import com.guxian.system.mapper.SysDictTypeMapper;
import com.guxian.system.service.SysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public IPage<SysDictType> page(Page<SysDictType> page, LambdaQueryWrapper<SysDictType> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 新增时若同编码字典类型已被逻辑删除（unique(uk_dict_type) 仍占用），
     * 先复活旧行，避免"删除后用同一编码重新新增"直接抛 Duplicate entry 500
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(SysDictType entity) {
        if (entity.getId() == null && StringUtils.hasText(entity.getDictType())) {
            SysDictType deleted = baseMapper.selectDeletedByDictType(entity.getDictType());
            if (deleted != null) {
                baseMapper.restoreById(deleted.getId());
                entity.setId(deleted.getId());
            }
        }
        return super.saveOrUpdate(entity);
    }
}
