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
    public void saveCustomer(TCustomerDTO dto) {
        TCustomer customer = new TCustomer();
        customer.setCustomerId(dto.getCustomerId());
        customer.setCustomerName(dto.getCustomerName());
        customer.setContact(dto.getContact());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setRemark(dto.getRemark());

        if (dto.getCustomerId() == null) {
            //新增
            this.save(customer);
        } else {
            //编辑
            this.updateById(customer);
        }
    }
}
