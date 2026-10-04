package com.guxian.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.sales.dto.TProductDTO;
import com.guxian.sales.entity.TProduct;

import java.util.List;

public interface TProductService extends IService<TProduct> {

    /** 产品分页 */
    IPage<TProductDTO> pageProduct(Page<TProduct> page, String productName, String productType);

    /** 新增/编辑产品 */
    void saveProduct(TProductDTO dto);

    /** 启用产品下拉（订单录入选择） */
    List<TProduct> listEnabled();
}
