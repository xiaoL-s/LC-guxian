package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysPrintTemplateDTO;
import com.guxian.system.entity.SysPrintTemplate;
import com.guxian.system.mapper.SysPrintTemplateMapper;
import com.guxian.system.service.SysPrintTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysPrintTemplateServiceImpl
        extends ServiceImpl<SysPrintTemplateMapper, SysPrintTemplate>
        implements SysPrintTemplateService {

    @Override
    public IPage<SysPrintTemplate> getPage(Page<SysPrintTemplate> page,
                                           String templateName,
                                           String templateType,
                                           Integer status) {
        LambdaQueryWrapper<SysPrintTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(templateName), SysPrintTemplate::getTemplateName, templateName);
        wrapper.eq(StringUtils.hasText(templateType), SysPrintTemplate::getTemplateType, templateType);
        wrapper.eq(status != null, SysPrintTemplate::getStatus, status);
        wrapper.orderByDesc(SysPrintTemplate::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplate(SysPrintTemplateDTO dto) {
        SysPrintTemplate template = new SysPrintTemplate();
        template.setTemplateId(dto.getTemplateId());
        template.setTemplateName(dto.getTemplateName());
        template.setTemplateType(dto.getTemplateType());
        template.setPaperSize(dto.getPaperSize());
        template.setTemplateContent(dto.getTemplateContent());
        template.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() == 1 ? 1 : 0);
        template.setStatus(dto.getStatus() != null && dto.getStatus() == 1 ? 1 : 0);

        if (template.getTemplateId() == null) {
            // 新增：若该单据类型下还没有默认模板，则本条自动成为默认模板
            // （否则用户在模板管理里"新增"了模板却不会被打印采用，容易误解为"改了不生效"）
            if (StringUtils.hasText(template.getTemplateType())) {
                Long defaultCount = baseMapper.selectCount(new LambdaQueryWrapper<SysPrintTemplate>()
                        .eq(SysPrintTemplate::getTemplateType, template.getTemplateType())
                        .eq(SysPrintTemplate::getIsDefault, 1));
                if (defaultCount == null || defaultCount == 0) {
                    template.setIsDefault(1);
                }
            }
            this.save(template);
        } else {
            // 编辑
            this.updateById(template);
        }

        // 同单据类型下默认模板互斥：若当前模板被设为默认，则把同类型其它模板的默认标记清掉
        if (Integer.valueOf(1).equals(template.getIsDefault())
                && StringUtils.hasText(template.getTemplateType())) {
            LambdaUpdateWrapper<SysPrintTemplate> clear = new LambdaUpdateWrapper<>();
            clear.orderByAsc(SysPrintTemplate::getTemplateId);
            clear.eq(SysPrintTemplate::getTemplateType, template.getTemplateType())
                 .ne(template.getTemplateId() != null, SysPrintTemplate::getTemplateId, template.getTemplateId())
                 .set(SysPrintTemplate::getIsDefault, 0);
            baseMapper.update(null, clear);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long templateId, Integer status) {
        SysPrintTemplate t = new SysPrintTemplate();
        t.setTemplateId(templateId);
        t.setStatus(status != null && status == 1 ? 1 : 0);
        this.updateById(t);
    }

    @Override
    public String getPreviewHtml(Long templateId) {
        SysPrintTemplate template = this.getById(templateId);
        return template == null ? "" : template.getTemplateContent();
    }

    @Override
    public List<String> listTypes() {
        return baseMapper.listDistinctTypes();
    }
}
