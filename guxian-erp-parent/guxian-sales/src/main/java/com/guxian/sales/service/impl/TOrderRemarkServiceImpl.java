package com.guxian.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.context.UserContext;
import com.guxian.model.LoginUser;
import com.guxian.sales.entity.TOrderRemark;
import com.guxian.sales.mapper.TOrderRemarkMapper;
import com.guxian.sales.service.TOrderRemarkService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 订单备注记录服务实现
 */
@Service
public class TOrderRemarkServiceImpl extends ServiceImpl<TOrderRemarkMapper, TOrderRemark> implements TOrderRemarkService {

    @Override
    public List<TOrderRemark> listByOrder(Long orderId) {
        LambdaQueryWrapper<TOrderRemark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TOrderRemark::getOrderId, orderId)
                .eq(TOrderRemark::getDelFlag, 0)
                // 最新备注在最上面
                .orderByDesc(TOrderRemark::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public TOrderRemark saveRemark(Long orderId, String remark) {
        // 内容校验：空/超长直接拒绝
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (!StringUtils.hasText(remark)) {
            throw new IllegalArgumentException("备注内容不能为空");
        }
        if (remark.length() > 500) {
            throw new IllegalArgumentException("备注内容不能超过500字");
        }
        TOrderRemark entity = new TOrderRemark();
        entity.setOrderId(orderId);
        entity.setRemark(remark.trim());
        entity.setDelFlag(0);
        // 记录操作人姓名（ID 由公共字段自动填充）
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null) {
            entity.setCreateByName(loginUser.getRealName());
        }
        this.save(entity);
        return entity;
    }
}
