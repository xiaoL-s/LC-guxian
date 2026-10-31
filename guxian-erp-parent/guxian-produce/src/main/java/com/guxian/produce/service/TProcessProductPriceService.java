package com.guxian.produce.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guxian.produce.entity.TProcessProductPrice;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 工序产品计件价 Service
 */
public interface TProcessProductPriceService {

    /** 分页查询（可按工序、产品名过滤） */
    Page<TProcessProductPrice> page(int pageNum, int pageSize, Long processId, String productName);

    /** 新增/修改单条 */
    void savePrice(TProcessProductPrice price);

    /** 删除单条（逻辑删） */
    void deletePrice(Long id);

    /** Excel 批量导入：列结构 工序名称|产品名称|计件单价（表头忽略），同一工序+产品覆盖更新 */
    String importExcel(MultipartFile file) throws IOException;

    /** 按指定工序导入：文件只需两列 产品名称|计件单价，全部归到该工序下 */
    String importByProcess(Long processId, MultipartFile file) throws IOException;

    /** 导出 Excel：按当前筛选条件导出 */
    byte[] exportExcel(Long processId, String productName) throws IOException;

    /** 报工计价：按工序+产品名查计件单价，查不到返回 null（调用方回退工序统一价） */
    BigDecimal findPrice(Long processId, String productName);

    /** 模板下载：返回空表头的 xlsx */
    byte[] template() throws IOException;
}
