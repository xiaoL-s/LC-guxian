package com.guxian.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.customer.dto.TCustomerFollowDTO;
import com.guxian.customer.entity.TCustomerFollow;

public interface TCustomerFollowService extends IService<TCustomerFollow> {
    IPage<TCustomerFollowDTO> page(IPage<TCustomerFollow> page, Long customerId);
    void saveFollow(TCustomerFollowDTO dto);
}
