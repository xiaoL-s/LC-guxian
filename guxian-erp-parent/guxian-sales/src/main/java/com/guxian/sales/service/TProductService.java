package com.guxian.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.sales.dto.MaterialOptionDTO;
import com.guxian.sales.dto.ProductBomDTO;
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

    /** 查询某产品的BOM（含物料信息） */
    List<ProductBomDTO> listBom(Long productId);

    /** 保存产品BOM（整单覆盖：先逻辑删旧，再插新） */
    void saveBom(Long productId, List<ProductBomDTO> bomList);

    /** 全部启用物料下拉（BOM配置选择） */
    List<MaterialOptionDTO> listMaterials();
}
