package com.guxian.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.exception.BusinessException;
import com.guxian.stock.entity.TShelf;
import com.guxian.stock.mapper.TShelfMapper;
import com.guxian.stock.service.TShelfService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TShelfServiceImpl extends ServiceImpl<TShelfMapper, TShelf> implements TShelfService {

    @Override
    public List<TShelf> listEnabled() {
        LambdaQueryWrapper<TShelf> w = new LambdaQueryWrapper<>();
        w.eq(TShelf::getStatus, 1).orderByAsc(TShelf::getSort);
        return baseMapper.selectList(w);
    }

    @Override
    public void saveShelf(TShelf shelf) {
        if (!StringUtils.hasText(shelf.getShelfName())) {
            throw new BusinessException("货架名称不能为空");
        }
        if (shelf.getShelfId() == null) {
            if (!StringUtils.hasText(shelf.getShelfCode())) {
                shelf.setShelfCode(String.valueOf(System.currentTimeMillis() % 100000));
            }
            if (shelf.getStatus() == null) {
                shelf.setStatus(1);
            }
            baseMapper.insert(shelf);
        } else {
            baseMapper.updateById(shelf);
        }
    }
}
