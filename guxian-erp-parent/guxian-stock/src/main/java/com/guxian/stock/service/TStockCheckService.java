package com.guxian.stock.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.stock.entity.TStockCheck;
import com.guxian.stock.entity.TStockCheckItem;

import java.util.List;
import java.util.Map;

public interface TStockCheckService extends IService<TStockCheck> {

    /** 创建盘点单（自动带出启用物料 + 账面库存） */
    Long createCheck(String remark);

    /** 录入实盘数（草稿态可反复保存） */
    void saveItems(Long checkId, List<Map<String, Object>> items);

    /** 过账：写盘点调整流水 + 修正库存 + 单据置为已过账 */
    void confirm(Long checkId);

    IPage<TStockCheck> pageCheck(Page<TStockCheck> page, Integer status, String keyword);

    /** 盘点单详情：头 + 明细 */
    Map<String, Object> detail(Long checkId);

    /** 明细列表（带物料信息） */
    List<TStockCheckItem> itemsOf(Long checkId);
}
