package com.guxian.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.customer.dto.TCustomerFollowDTO;
import com.guxian.customer.entity.TCustomerFollow;
import com.guxian.customer.mapper.TCustomerFollowMapper;
import com.guxian.customer.service.TCustomerFollowService;
import com.guxian.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TCustomerFollowServiceImpl extends ServiceImpl<TCustomerFollowMapper, TCustomerFollow> implements TCustomerFollowService {

    @Override
    public IPage<TCustomerFollowDTO> page(IPage<TCustomerFollow> page, Long customerId) {
        LambdaQueryWrapper<TCustomerFollow> wrapper = new LambdaQueryWrapper<>();
        // customerId 可选：不传时返回全部跟进记录（客户跟进记录菜单可独立查看）
        wrapper.eq(customerId != null, TCustomerFollow::getCustomerId, customerId);
        wrapper.orderByDesc(TCustomerFollow::getFollowTime);
        IPage<TCustomerFollow> pageData = baseMapper.selectPage(page, wrapper);
        return pageData.convert(this::toDto);
    }

    private TCustomerFollowDTO toDto(TCustomerFollow entity) {
        TCustomerFollowDTO dto = new TCustomerFollowDTO();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setFollowContent(entity.getFollowContent());
        dto.setFollowTime(entity.getFollowTime());
        dto.setFollowUser(entity.getFollowUser());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFollow(TCustomerFollowDTO dto) {
        TCustomerFollow follow = new TCustomerFollow();
        follow.setId(dto.getId());
        follow.setCustomerId(dto.getCustomerId());
        follow.setFollowContent(dto.getFollowContent());
        follow.setFollowTime(dto.getFollowTime());
        follow.setFollowUser(dto.getFollowUser());
        if(dto.getId() == null){
            this.save(follow);
        }else{
            this.updateById(follow);
        }
    }
}
