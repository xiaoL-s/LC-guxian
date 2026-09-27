package com.guxian.stock.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.stock.dto.StockBillDTO;
import com.guxian.stock.dto.StockChangeDTO;
import com.guxian.stock.entity.TMaterialStock;
import com.guxian.stock.entity.TStockRecord;

import java.math.BigDecimal;
import java.util.List;

public interface TMaterialStockService extends IService<TMaterialStock> {

    IPage<TMaterialStock> pageMaterial(Page<TMaterialStock> page, String keyword, Integer materialType, Integer enable);

    /** 启用物料列表（下拉用） */
    List<TMaterialStock> listEnabled();

    /** 保存物料（新增/编辑），编码唯一 */
    void saveMaterial(TMaterialStock material);

    /** 库存变动：入库/出库/盘点，写流水并更新库存 */
    void changeStock(StockChangeDTO dto);

    /** 批量出库（生产领料），校验库存足够 */
    void batchOutStock(List<StockChangeDTO> items);

    IPage<TStockRecord> pageRecord(Page<TStockRecord> page, Long materialId, Integer bizType, String relateNo);

    /** 出入库单据：创建并过账（采购入库2/退货出库5），返回单号 */
    String createBill(StockBillDTO dto);

    /** 库存预警列表：当前库存低于预警值 */
    List<TMaterialStock> listWarn();
}
