package com.guxian.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.exception.BusinessException;
import com.guxian.stock.dto.BillItemDTO;
import com.guxian.stock.dto.StockBillDTO;
import com.guxian.stock.dto.StockChangeDTO;
import com.guxian.stock.entity.TMaterialStock;
import com.guxian.stock.entity.TStockRecord;
import com.guxian.stock.mapper.TMaterialStockMapper;
import com.guxian.stock.mapper.TStockRecordMapper;
import com.guxian.stock.service.TMaterialStockService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TMaterialStockServiceImpl extends ServiceImpl<TMaterialStockMapper, TMaterialStock> implements TMaterialStockService {

    @Resource
    private TStockRecordMapper stockRecordMapper;

    @Override
    public IPage<TMaterialStock> pageMaterial(Page<TMaterialStock> page, String keyword, Integer materialType, Integer enable) {
        LambdaQueryWrapper<TMaterialStock> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(x -> x.like(TMaterialStock::getMaterialCode, keyword)
                    .or().like(TMaterialStock::getMaterialName, keyword)
                    .or().like(TMaterialStock::getSpec, keyword));
        }
        w.eq(materialType != null, TMaterialStock::getMaterialType, materialType);
        w.eq(enable != null, TMaterialStock::getEnable, enable);
        w.orderByAsc(TMaterialStock::getMaterialCode);
        return baseMapper.selectPage(page, w);
    }

    @Override
    public List<TMaterialStock> listEnabled() {
        LambdaQueryWrapper<TMaterialStock> w = new LambdaQueryWrapper<>();
        w.eq(TMaterialStock::getEnable, 1).orderByAsc(TMaterialStock::getMaterialCode);
        return baseMapper.selectList(w);
    }

    @Override
    public void saveMaterial(TMaterialStock material) {
        if (!StringUtils.hasText(material.getMaterialCode())) {
            throw new BusinessException("物料编码不能为空");
        }
        if (!StringUtils.hasText(material.getMaterialName())) {
            throw new BusinessException("物料名称不能为空");
        }
        if (material.getMaterialType() == null) {
            throw new BusinessException("请选择物料类型");
        }
        // 编码唯一校验
        LambdaQueryWrapper<TMaterialStock> w = new LambdaQueryWrapper<>();
        w.eq(TMaterialStock::getMaterialCode, material.getMaterialCode());
        if (material.getId() != null) {
            w.ne(TMaterialStock::getId, material.getId());
        }
        if (baseMapper.selectCount(w) > 0) {
            throw new BusinessException("物料编码已存在：" + material.getMaterialCode());
        }
        if (material.getStockNum() == null) {
            material.setStockNum(BigDecimal.ZERO);
        }
        if (material.getWarnNum() == null) {
            material.setWarnNum(BigDecimal.ZERO);
        }
        if (material.getEnable() == null) {
            material.setEnable(1);
        }
        if (material.getId() == null) {
            baseMapper.insert(material);
        } else {
            baseMapper.updateById(material);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStock(StockChangeDTO dto) {
        TMaterialStock material = mustGet(dto.getMaterialId());
        BigDecimal change = dto.getChangeNum() == null ? BigDecimal.ZERO : dto.getChangeNum();
        if (change.signum() == 0) {
            throw new BusinessException("变动数量不能为 0");
        }
        // 出库校验库存足够
        BigDecimal after = nz(material.getStockNum()).add(change);
        if (after.signum() < 0) {
            throw new BusinessException("库存不足，当前库存 " + nz(material.getStockNum()) + " " + material.getUnit());
        }
        after = after.setScale(3, RoundingMode.HALF_UP);
        TMaterialStock upd = new TMaterialStock();
        upd.setId(material.getId());
        upd.setStockNum(after);
        baseMapper.updateById(upd);
        // 写流水
        TStockRecord record = new TStockRecord();
        record.setMaterialId(material.getId());
        record.setMaterialCode(material.getMaterialCode());
        record.setMaterialName(material.getMaterialName());
        record.setBizType(dto.getBizType() == null ? 4 : dto.getBizType());
        record.setInNum(change.signum() > 0 ? change : BigDecimal.ZERO);
        record.setOutNum(change.signum() < 0 ? change.abs() : BigDecimal.ZERO);
        record.setAfterNum(after);
        record.setRelateType(dto.getRelateType());
        record.setRelateNo(dto.getRelateNo());
        record.setRemark(dto.getRemark());
        stockRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOutStock(List<StockChangeDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("领料明细不能为空");
        }
        for (StockChangeDTO item : items) {
            item.setChangeNum(nz(item.getChangeNum()).negate());
            if (item.getBizType() == null) {
                item.setBizType(3); // 生产领料
            }
            changeStock(item);
        }
    }

    @Override
    public IPage<TStockRecord> pageRecord(Page<TStockRecord> page, Long materialId, Integer bizType, String relateNo) {
        LambdaQueryWrapper<TStockRecord> w = new LambdaQueryWrapper<>();
        w.eq(materialId != null, TStockRecord::getMaterialId, materialId);
        w.eq(bizType != null, TStockRecord::getBizType, bizType);
        w.like(StringUtils.hasText(relateNo), TStockRecord::getRelateNo, relateNo);
        w.orderByDesc(TStockRecord::getId);
        return stockRecordMapper.selectPage(page, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createBill(StockBillDTO dto) {
        if (dto.getBizType() == null || (dto.getBizType() != 2 && dto.getBizType() != 5)) {
            throw new BusinessException("单据类型仅支持：2采购入库 / 5退货出库");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("单据明细不能为空");
        }
        String billNo = generateBillNo(dto.getBizType());
        int sign = dto.getBizType() == 2 ? 1 : -1;
        for (BillItemDTO item : dto.getItems()) {
            if (item.getMaterialId() == null) {
                throw new BusinessException("单据明细缺少物料");
            }
            BigDecimal num = item.getNum() == null ? BigDecimal.ZERO : item.getNum();
            if (num.signum() <= 0) {
                throw new BusinessException("单据明细数量必须大于 0");
            }
            TMaterialStock material = mustGet(item.getMaterialId());
            BigDecimal after = nz(material.getStockNum()).add(num.multiply(BigDecimal.valueOf(sign)));
            if (after.signum() < 0) {
                throw new BusinessException("库存不足，当前库存 " + nz(material.getStockNum()) + " " + material.getUnit());
            }
            after = after.setScale(3, RoundingMode.HALF_UP);
            TMaterialStock upd = new TMaterialStock();
            upd.setId(material.getId());
            upd.setStockNum(after);
            baseMapper.updateById(upd);
            // 单据流水
            TStockRecord record = new TStockRecord();
            record.setMaterialId(material.getId());
            record.setMaterialCode(material.getMaterialCode());
            record.setMaterialName(material.getMaterialName());
            record.setBizType(dto.getBizType());
            record.setInNum(sign > 0 ? num : BigDecimal.ZERO);
            record.setOutNum(sign < 0 ? num : BigDecimal.ZERO);
            record.setAfterNum(after);
            record.setRelateType("BILL");
            record.setRelateNo(billNo);
            record.setRemark(item.getRemark() == null ? dto.getRemark() : item.getRemark());
            stockRecordMapper.insert(record);
        }
        return billNo;
    }

    @Override
    public List<TMaterialStock> listWarn() {
        LambdaQueryWrapper<TMaterialStock> w = new LambdaQueryWrapper<>();
        w.gt(TMaterialStock::getWarnNum, 0)
                .apply("stock_num < warn_num")
                .orderByAsc(TMaterialStock::getMaterialCode);
        return baseMapper.selectList(w);
    }

    private synchronized String generateBillNo(int bizType) {
        String prefix = (bizType == 2 ? "IN" : "OUT") + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<TStockRecord> w = new LambdaQueryWrapper<>();
        w.eq(TStockRecord::getRelateType, "BILL").likeRight(TStockRecord::getRelateNo, prefix)
                .orderByDesc(TStockRecord::getRelateNo).last("LIMIT 1");
        TStockRecord last = stockRecordMapper.selectOne(w);
        int seq = 1;
        if (last != null && last.getRelateNo() != null && last.getRelateNo().length() >= prefix.length()) {
            try {
                seq = Integer.parseInt(last.getRelateNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private TMaterialStock mustGet(Long id) {
        TMaterialStock material = baseMapper.selectById(id);
        if (material == null) {
            throw new BusinessException("物料不存在");
        }
        return material;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
