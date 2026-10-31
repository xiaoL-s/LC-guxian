package com.guxian.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.sales.entity.TOrderRemark;

import java.util.List;

/**
 * 订单备注记录服务：按订单查询/新增工作人员备注
 */
public interface TOrderRemarkService extends IService<TOrderRemark> {

    /** 按订单查询备注列表（创建时间倒序，仅未删除） */
    List<TOrderRemark> listByOrder(Long orderId);

    /** 新增备注：记录当前登录人姓名 */
    TOrderRemark saveRemark(Long orderId, String remark);
}
