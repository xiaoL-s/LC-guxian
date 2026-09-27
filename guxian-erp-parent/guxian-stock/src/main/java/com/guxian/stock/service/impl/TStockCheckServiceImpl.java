package com.guxian.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.context.UserContext;
import com.guxian.exception.BusinessException;
import com.guxian.stock.entity.TMaterialStock;
import com.guxian.stock.entity.TStockCheck;
import com.guxian.stock.entity.TStockCheckItem;
import com.guxian.stock.entity.TStockRecord;
import com.guxian.stock.mapper.TMaterialStockMapper;
import com.guxian.stock.mapper.TStockCheckItemMapper;
import com.guxian.stock.mapper.TStockCheckMapper;
import com.guxian.stock.mapper.TStockRecordMapper;
import com.guxian.stock.service.TStockCheckService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TStockCheckServiceImpl extends ServiceImpl<TStockCheckMapper, TStockCheck> implements TStockCheckService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_DONE = 1;

    @Resource
    private TStockCheckItemMapper checkItemMapper;
    @Resource
    private TMaterialStockMapper materialStockMapper;
    @Resource
    private TStockRecordMapper stockRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheck(String remark) {
        TStockCheck check = new TStockCheck();
        check.setCheckNo(generateCheckNo());
        check.setCheckStatus(STATUS_DRAFT);
        check.setTotalDiff(BigDecimal.ZERO);
        check.setRemark(remark);
        baseMapper.insert(check);
        // 带出全部启用物料
        List<TMaterialStock> materials = materialStockMapper.selectList(new LambdaQueryWrapper<TMaterialStock>()
                .eq(TMaterialStock::getEnable, 1).orderByAsc(TMaterialStock::getMaterialCode));
        for (TMaterialStock m : materials) {
            TStockCheckItem item = new TStockCheckItem();
            item.setCheckId(check.getCheckId());
            item.setMaterialId(m.getId());
            item.setMaterialCode(m.getMaterialCode());
            item.setMaterialName(m.getMaterialName());
            item.setUnit(m.getUnit());
            item.setBookNum(nz(m.getStockNum()));
            item.setRealNum(null);
            item.setDiffNum(BigDecimal.ZERO);
            checkItemMapper.insert(item);
        }
        return check.getCheckId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveItems(Long checkId, List<Map<String, Object>> items) {
        TStockCheck check = mustGet(checkId);
        if (check.getCheckStatus() != STATUS_DRAFT) {
            throw new BusinessException("盘点单已过账，不能修改");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("盘点明细不能为空");
        }
        for (Map<String, Object> in : items) {
            Long materialId = ((Number) in.get("materialId")).longValue();
            BigDecimal realNum = in.get("realNum") == null ? null : new BigDecimal(in.get("realNum").toString());
            if (realNum == null || realNum.signum() < 0) {
                throw new BusinessException("实盘数不能为空且不能为负");
            }
            TStockCheckItem item = checkItemMapper.selectOne(new LambdaQueryWrapper<TStockCheckItem>()
                    .eq(TStockCheckItem::getCheckId, checkId)
                    .eq(TStockCheckItem::getMaterialId, materialId));
            if (item == null) {
                throw new BusinessException("物料不在盘点单中");
            }
            TStockCheckItem upd = new TStockCheckItem();
            upd.setId(item.getId());
            upd.setRealNum(realNum.setScale(3, RoundingMode.HALF_UP));
            upd.setDiffNum(realNum.subtract(nz(item.getBookNum())).setScale(3, RoundingMode.HALF_UP));
            checkItemMapper.updateById(upd);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long checkId) {
        TStockCheck check = mustGet(checkId);
        if (check.getCheckStatus() != STATUS_DRAFT) {
            throw new BusinessException("盘点单已过账，请勿重复操作");
        }
        List<TStockCheckItem> items = checkItemMapper.selectList(new LambdaQueryWrapper<TStockCheckItem>()
                .eq(TStockCheckItem::getCheckId, checkId));
        if (items.isEmpty()) {
            throw new BusinessException("盘点单无明细");
        }
        BigDecimal totalDiff = BigDecimal.ZERO;
        for (TStockCheckItem item : items) {
            if (item.getRealNum() == null) {
                throw new BusinessException("尚有物料未录入实盘数：" + item.getMaterialName());
            }
            BigDecimal diff = nz(item.getDiffNum());
            if (diff.signum() == 0) {
                continue;
            }
            // 原子修正库存（防止负数）
            int rows = materialStockMapper.adjustStock(item.getMaterialId(), diff);
            if (rows == 0) {
                throw new BusinessException("库存修正失败（可能为负）：" + item.getMaterialName());
            }
            // 盘点调整流水 bizType=4
            TStockRecord record = new TStockRecord();
            record.setMaterialId(item.getMaterialId());
            record.setMaterialCode(item.getMaterialCode());
            record.setMaterialName(item.getMaterialName());
            record.setBizType(4);
            record.setInNum(diff.signum() > 0 ? diff : BigDecimal.ZERO);
            record.setOutNum(diff.signum() < 0 ? diff.abs() : BigDecimal.ZERO);
            record.setAfterNum(nz(item.getBookNum()).add(diff));
            record.setRelateType("CHECK");
            record.setRelateNo(check.getCheckNo());
            record.setRemark("盘点调整");
            stockRecordMapper.insert(record);
            totalDiff = totalDiff.add(diff);
        }
        TStockCheck upd = new TStockCheck();
        upd.setCheckId(checkId);
        upd.setCheckStatus(STATUS_DONE);
        upd.setTotalDiff(totalDiff.setScale(3, RoundingMode.HALF_UP));
        baseMapper.updateById(upd);
    }

    @Override
    public IPage<TStockCheck> pageCheck(Page<TStockCheck> page, Integer status, String keyword) {
        LambdaQueryWrapper<TStockCheck> w = new LambdaQueryWrapper<>();
        w.eq(status != null, TStockCheck::getCheckStatus, status);
        w.like(StringUtils.hasText(keyword), TStockCheck::getCheckNo, keyword);
        w.orderByDesc(TStockCheck::getCheckId);
        return baseMapper.selectPage(page, w);
    }

    @Override
    public Map<String, Object> detail(Long checkId) {
        TStockCheck check = mustGet(checkId);
        Map<String, Object> vo = new HashMap<>();
        vo.put("checkId", check.getCheckId());
        vo.put("checkNo", check.getCheckNo());
        vo.put("checkStatus", check.getCheckStatus());
        vo.put("totalDiff", check.getTotalDiff());
        vo.put("remark", check.getRemark());
        vo.put("createTime", check.getCreateTime() == null ? "" : check.getCreateTime().toString());
        vo.put("items", itemsOf(checkId));
        return vo;
    }

    @Override
    public List<TStockCheckItem> itemsOf(Long checkId) {
        return checkItemMapper.selectList(new LambdaQueryWrapper<TStockCheckItem>()
                .eq(TStockCheckItem::getCheckId, checkId)
                .orderByAsc(TStockCheckItem::getId));
    }

    private TStockCheck mustGet(Long checkId) {
        TStockCheck check = baseMapper.selectById(checkId);
        if (check == null) {
            throw new BusinessException("盘点单不存在");
        }
        return check;
    }

    private synchronized String generateCheckNo() {
        String prefix = "CK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<TStockCheck> w = new LambdaQueryWrapper<>();
        w.likeRight(TStockCheck::getCheckNo, prefix).orderByDesc(TStockCheck::getCheckNo).last("LIMIT 1");
        TStockCheck last = baseMapper.selectOne(w);
        int seq = 1;
        if (last != null && last.getCheckNo() != null && last.getCheckNo().length() >= prefix.length()) {
            try {
                seq = Integer.parseInt(last.getCheckNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
