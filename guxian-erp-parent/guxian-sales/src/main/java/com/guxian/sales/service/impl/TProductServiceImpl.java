package com.guxian.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.exception.BusinessException;
import com.guxian.sales.dto.TProductDTO;
import com.guxian.sales.entity.TProduct;
import com.guxian.sales.mapper.TProductMapper;
import com.guxian.sales.service.TProductService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TProductServiceImpl extends ServiceImpl<TProductMapper, TProduct> implements TProductService {

    @Override
    public IPage<TProductDTO> pageProduct(Page<TProduct> page, String productName, String productType) {
        LambdaQueryWrapper<TProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(productName), TProduct::getProductName, productName);
        wrapper.eq(StringUtils.hasText(productType), TProduct::getProductType, productType);
        wrapper.orderByDesc(TProduct::getProductId);
        IPage<TProduct> productPage = baseMapper.selectPage(page, wrapper);
        return productPage.convert(p -> {
            TProductDTO dto = new TProductDTO();
            BeanUtils.copyProperties(p, dto);
            return dto;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(TProductDTO dto) {
        TProduct product = new TProduct();
        BeanUtils.copyProperties(dto, product);
        // 编码唯一性校验
        LambdaQueryWrapper<TProduct> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(TProduct::getProductCode, dto.getProductCode());
        if (dto.getProductId() != null) {
            codeWrapper.ne(TProduct::getProductId, dto.getProductId());
        }
        if (baseMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException("产品编码已存在：" + dto.getProductCode());
        }
        // 新增时若同编码产品已被逻辑删除（unique(uk_product_code) 仍占用），
        // 先复活旧行，避免"删除产品后用同一编码重新新增"直接抛 Duplicate entry 500
        if (dto.getProductId() == null && StringUtils.hasText(dto.getProductCode())) {
            TProduct deleted = baseMapper.selectDeletedByProductCode(dto.getProductCode());
            if (deleted != null) {
                baseMapper.restoreById(deleted.getProductId());
                product.setProductId(deleted.getProductId());
            }
        }
        if (product.getProductId() == null) {
            this.save(product);
        } else {
            this.updateById(product);
        }
    }

    @Override
    public List<TProduct> listEnabled() {
        LambdaQueryWrapper<TProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TProduct::getStatus, 1).orderByDesc(TProduct::getProductId);
        return this.list(wrapper);
    }
}
