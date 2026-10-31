package com.guxian.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.customer.dto.TCustomerDTO;
import com.guxian.customer.entity.TCustomer;
import com.guxian.customer.vo.TCustomerQueryVO;

public interface TCustomerService extends IService<TCustomer> {

    IPage<TCustomerDTO> pageCustomer(Page<TCustomer> page, String customerName, String phone);
    /** 新增/编辑客户；新增时返回新客户ID，编辑时返回原ID */
    Long saveCustomer(TCustomerDTO dto);
}
