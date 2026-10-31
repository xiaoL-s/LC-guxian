package com.guxian.produce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.exception.BusinessException;
import com.guxian.produce.entity.TProcessDict;
import com.guxian.produce.entity.TProcessProductPrice;
import com.guxian.produce.mapper.ProductReadMapper;
import com.guxian.produce.mapper.TProcessDictMapper;
import com.guxian.produce.mapper.TProcessProductPriceMapper;
import com.guxian.produce.service.TProcessProductPriceService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工序产品计件价 Service 实现
 * Excel 列结构（第1行表头）：工序名称 | 产品名称 | 计件单价(元)
 */
@Service
public class TProcessProductPriceServiceImpl implements TProcessProductPriceService {

    @Resource
    private TProcessProductPriceMapper priceMapper;

    @Resource
    private TProcessDictMapper processDictMapper;

    @Resource
    private ProductReadMapper productReadMapper;

    /**
     * 以字典管理里的全部产品为基准，左拼当前工序已配置的计件价。
     * 这样弹窗里能看到所有产品，没配置价的显示 0，配置过的显示已配置值。
     */
    @Override
    public Page<TProcessProductPrice> page(int pageNum, int pageSize, Long processId, String productName) {
        // 全部产品名（字典 style_ 开头）
        List<String> allProducts = productReadMapper.selectAllProductNames();
        // 当前工序已配置的价格
        LambdaQueryWrapper<TProcessProductPrice> qw = new LambdaQueryWrapper<>();
        if (processId != null) qw.eq(TProcessProductPrice::getProcessId, processId);
        if (productName != null && !productName.trim().isEmpty()) {
            allProducts = allProducts.stream()
                    .filter(p -> p.contains(productName.trim())).toList();
        }
        List<TProcessProductPrice> configured = priceMapper.selectList(qw);
        java.util.Map<String, BigDecimal> confMap = new java.util.HashMap<>();
        java.util.Map<String, Long> idMap = new java.util.HashMap<>();
        for (TProcessProductPrice p : configured) {
            confMap.put(p.getProductName(), p.getPrice());
            idMap.put(p.getProductName(), p.getId());
        }
        // 组装结果
        List<TProcessProductPrice> merged = new java.util.ArrayList<>();
        for (String name : allProducts) {
            TProcessProductPrice row = new TProcessProductPrice();
            row.setProcessId(processId);
            row.setProductName(name);
            row.setPrice(confMap.getOrDefault(name, BigDecimal.ZERO));
            row.setId(idMap.get(name)); // null 表示未配置
            merged.add(row);
        }
        // 手动分页
        int total = merged.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(total, from + pageSize);
        Page<TProcessProductPrice> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(from >= total ? java.util.Collections.emptyList() : merged.subList(from, to));
        return result;
    }

    @Override
    public void savePrice(TProcessProductPrice price) {
        if (price.getId() != null) {
            priceMapper.updateById(price);
        } else {
            // 同工序同产品已存在则更新，避免唯一索引冲突
            LambdaQueryWrapper<TProcessProductPrice> qw = new LambdaQueryWrapper<TProcessProductPrice>()
                    .eq(TProcessProductPrice::getProcessId, price.getProcessId())
                    .eq(TProcessProductPrice::getProductName, price.getProductName());
            TProcessProductPrice exist = priceMapper.selectOne(qw);
            if (exist != null) {
                exist.setPrice(price.getPrice());
                priceMapper.updateById(exist);
            } else {
                priceMapper.insert(price);
            }
        }
    }

    @Override
    public void deletePrice(Long id) {
        priceMapper.deleteById(id);
    }

    @Override
    public String importByProcess(Long processId, MultipartFile file) throws IOException {
        TProcessDict process = processDictMapper.selectById(processId);
        if (process == null) throw new BusinessException("工序不存在");
        int insertCnt = 0, updateCnt = 0, skipCnt = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                // 自动识别列结构：
                // 三列格式（带工序名列）：第0列=工序名, 第1列=产品名, 第2列=单价
                // 两列格式：              第0列=产品名,           第1列=单价
                String col0 = getCellString(row.getCell(0)).trim();
                String productName;
                BigDecimal price;
                if (process.getProcessName().equals(col0)) {
                    productName = getCellString(row.getCell(1)).trim();
                    price = getCellDecimal(row.getCell(2));
                } else {
                    productName = col0;
                    price = getCellDecimal(row.getCell(1));
                }
                if (productName.isEmpty()) { skipCnt++; continue; }
                // 按工序+产品名 upsert
                LambdaQueryWrapper<TProcessProductPrice> qw = new LambdaQueryWrapper<TProcessProductPrice>()
                        .eq(TProcessProductPrice::getProcessId, processId)
                        .eq(TProcessProductPrice::getProductName, productName);
                TProcessProductPrice exist = priceMapper.selectOne(qw);
                if (exist != null) {
                    exist.setPrice(price);
                    priceMapper.updateById(exist);
                    updateCnt++;
                } else {
                    TProcessProductPrice p = new TProcessProductPrice();
                    p.setProcessId(processId);
                    p.setProductName(productName);
                    p.setPrice(price);
                    priceMapper.insert(p);
                    insertCnt++;
                }
            }
        }
        return String.format("导入完成（工序：%s）：新增 %d，更新 %d，跳过空行 %d",
                process.getProcessName(), insertCnt, updateCnt, skipCnt);
    }

    @Override
    public String importExcel(MultipartFile file) throws IOException {
        // 1. 工序名称 → processId 字典
        Map<String, Long> processNameMap = new HashMap<>();
        for (TProcessDict d : processDictMapper.selectList(null)) {
            processNameMap.put(d.getProcessName(), d.getProcessId());
        }
        int insertCnt = 0, updateCnt = 0, skipCnt = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) { // 跳过表头
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String processName = getCellString(row.getCell(0)).trim();
                String productName = getCellString(row.getCell(1)).trim();
                BigDecimal price = getCellDecimal(row.getCell(2));
                if (productName.isEmpty()) continue;
                Long pid = processNameMap.get(processName);
                if (pid == null) { skipCnt++; continue; }
                // 已存在则覆盖
                LambdaQueryWrapper<TProcessProductPrice> qw = new LambdaQueryWrapper<TProcessProductPrice>()
                        .eq(TProcessProductPrice::getProcessId, pid)
                        .eq(TProcessProductPrice::getProductName, productName);
                TProcessProductPrice exist = priceMapper.selectOne(qw);
                if (exist != null) {
                    exist.setPrice(price);
                    priceMapper.updateById(exist);
                    updateCnt++;
                } else {
                    TProcessProductPrice p = new TProcessProductPrice();
                    p.setProcessId(pid);
                    p.setProductName(productName);
                    p.setPrice(price);
                    priceMapper.insert(p);
                    insertCnt++;
                }
            }
        }
        return String.format("导入完成：新增 %d 条，更新 %d 条，跳过(工序不匹配/空行) %d 条", insertCnt, updateCnt, skipCnt);
    }

    @Override
    public byte[] exportExcel(Long processId, String productName) throws IOException {
        List<TProcessProductPrice> list = page(1, 100000, processId, productName).getRecords();
        // 工序ID → 名称
        Map<Long, String> pidMap = new HashMap<>();
        for (TProcessDict d : processDictMapper.selectList(null)) {
            pidMap.put(d.getProcessId(), d.getProcessName());
        }
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("产品计件价");
            // 表头
            Row head = sheet.createRow(0);
            String[] heads = {"工序名称", "产品名称", "计件单价(元)"};
            CellStyle headStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headStyle.setFont(bold);
            for (int c = 0; c < heads.length; c++) {
                Cell cell = head.createCell(c);
                cell.setCellValue(heads[c]);
                cell.setCellStyle(headStyle);
            }
            // 数据行
            int r = 1;
            for (TProcessProductPrice p : list) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(pidMap.getOrDefault(p.getProcessId(), ""));
                row.createCell(1).setCellValue(p.getProductName());
                row.createCell(2).setCellValue(p.getPrice() == null ? 0 : p.getPrice().doubleValue());
            }
            for (int c = 0; c < heads.length; c++) sheet.autoSizeColumn(c);
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public BigDecimal findPrice(Long processId, String productName) {
        if (processId == null || productName == null) return null;
        LambdaQueryWrapper<TProcessProductPrice> qw = new LambdaQueryWrapper<TProcessProductPrice>()
                .eq(TProcessProductPrice::getProcessId, processId)
                .eq(TProcessProductPrice::getProductName, productName)
                .last("LIMIT 1");
        TProcessProductPrice p = priceMapper.selectOne(qw);
        return p == null ? null : p.getPrice();
    }

    @Override
    public byte[] template() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("产品计件价");
            Row head = sheet.createRow(0);
            // 按工序导入的模板：两列（产品名称 | 计件单价）
            String[] heads = {"产品名称", "计件单价(元)"};
            for (int c = 0; c < heads.length; c++) head.createCell(c).setCellValue(heads[c]);
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("护童-压条款（120款）");
            r1.createCell(1).setCellValue(3.0);
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("小料三节");
            r2.createCell(1).setCellValue(2.0);
            wb.write(out);
            return out.toByteArray();
        }
    }

    /** 读取单元格文本（兼容数字/字符串） */
    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    /** 读取单元格数字 */
    private BigDecimal getCellDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        String s = getCellString(cell);
        if (s.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
