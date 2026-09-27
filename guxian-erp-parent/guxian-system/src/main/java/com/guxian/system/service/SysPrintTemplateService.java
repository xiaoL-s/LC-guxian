package com.guxian.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.dto.SysPrintTemplateDTO;
import com.guxian.system.entity.SysPrintTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface SysPrintTemplateService extends IService<SysPrintTemplate> {

    /**
     * 分页查询
     */
    IPage<SysPrintTemplate> getPage(Page<SysPrintTemplate> page,
                                    String templateName,
                                    String templateType,
                                    Integer status);

    /**
     * 新增 / 编辑（同单据类型下默认模板互斥）
     */
    void saveTemplate(SysPrintTemplateDTO dto);

    /**
     * 启用 / 禁用切换
     */
    void toggleStatus(Long templateId, Integer status);

    /**
     * 预览：返回模板原始内容
     */
    String getPreviewHtml(Long templateId);

    /**
     * 所有去重后的单据类型（下拉用）
     */
    List<String> listTypes();
}
