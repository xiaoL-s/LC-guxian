package com.guxian.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.customer.dto.TCustomerDTO;
import com.guxian.customer.entity.TCustomer;
import com.guxian.customer.mapper.TCustomerMapper;
import com.guxian.customer.service.TCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.annotation.Resource;

@Service
public class TCustomerServiceImpl extends ServiceImpl<TCustomerMapper, TCustomer> implements TCustomerService {

    @Override
    public IPage<TCustomerDTO> pageCustomer(Page<TCustomer> page, String customerName, String phone) {
        LambdaQueryWrapper<TCustomer> wrapper = new LambdaQueryWrapper<>();
        //客户名称模糊搜索
        wrapper.like(StringUtils.hasText(customerName), TCustomer::getCustomerName, customerName);
        //手机号模糊搜索
        wrapper.like(StringUtils.hasText(phone), TCustomer::getPhone, phone);
        //按客户ID倒序
        wrapper.orderByDesc(TCustomer::getCustomerId);
        IPage<TCustomer> customerPage = baseMapper.selectPage(page, wrapper);

        //entity转DTO
        return customerPage.convert(this::convertToDTO);
    }

    private TCustomerDTO convertToDTO(TCustomer entity) {
        TCustomerDTO dto = new TCustomerDTO();
        dto.setCustomerId(entity.getCustomerId());
        dto.setCustomerName(entity.getCustomerName());
        dto.setContact(entity.getContact());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setRemark(entity.getRemark());
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCustomer(TCustomerDTO dto) {
        TCustomer customer = new TCustomer();
        customer.setCustomerId(dto.getCustomerId());
        customer.setCustomerName(dto.getCustomerName());
        // 空字段给默认值，避免仅传客户名下单时因 NOT NULL 无默认值导致插入失败
        customer.setContact(dto.getContact() == null ? "" : dto.getContact());
        customer.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        customer.setAddress(dto.getAddress() == null ? "" : dto.getAddress());
        customer.setRemark(dto.getRemark() == null ? "" : dto.getRemark());

        if (dto.getCustomerId() == null) {
            //新增：保存后返回新客户ID（供下单页自动创建客户后直接关联订单）
            this.save(customer);
            return customer.getCustomerId();
        } else {
            //编辑
            this.updateById(customer);
            return dto.getCustomerId();
        }
    }
}
