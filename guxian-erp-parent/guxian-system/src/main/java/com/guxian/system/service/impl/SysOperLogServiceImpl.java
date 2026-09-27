package com.guxian.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysOperLogDTO;
import com.guxian.mybatis.entity.SysOperLog;
import com.guxian.mybatis.mapper.SysOperLogMapper;
import com.guxian.system.service.SysOperLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    // 新增时间解析工具方法（增加时区转换，解决8小时时差）
    private LocalDateTime parseDateTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            // 如果带Z，代表UTC时区，转成东八区北京时间
            if(timeStr.endsWith("Z")){
                ZonedDateTime utcTime = ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                // UTC +8小时 转为北京时间
                return utcTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            }
            // 不带Z，直接解析本地时间
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            // 兜底解析 yyyy-MM-dd HH:mm:ss
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @Override
    public IPage<SysOperLogDTO> page(IPage<SysOperLog> page, String operModule, String realName, String startTime, String endTime) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        //模块模糊搜索
        wrapper.like(StringUtils.hasText(operModule), SysOperLog::getOperModule, operModule);
        //操作人姓名模糊搜索
        wrapper.like(StringUtils.hasText(realName), SysOperLog::getRealName, realName);

        // ========== 时间范围查询 ==========
        if(StringUtils.hasText(startTime)){
            LocalDateTime start = parseDateTime(startTime);
            if(start != null){
                wrapper.ge(SysOperLog::getOperTime, start);
            }
        }
        if(StringUtils.hasText(endTime)){
            LocalDateTime end = parseDateTime(endTime);
            if(end != null){
                // 结束日期自动补到23:59:59，包含当天所有数据
                end = end.withHour(23).withMinute(59).withSecond(59);
                wrapper.le(SysOperLog::getOperTime, end);
            }
        }

        IPage<SysOperLog> logPage = baseMapper.selectPage(page, wrapper);
        Page<SysOperLogDTO> dtoPage = new Page<>();
        dtoPage.setTotal(logPage.getTotal());
        dtoPage.setRecords(logPage.getRecords().stream().map(item->{
            SysOperLogDTO dto = new SysOperLogDTO();
            dto.setId(item.getId());
            dto.setUsername(item.getUsername());
            dto.setRealName(item.getRealName());
            dto.setOperModule(item.getOperModule());
            dto.setOperType(item.getOperType());
            dto.setOperContent(item.getOperContent());
            dto.setIp(item.getIp());
            dto.setOperTime(item.getOperTime());
            return dto;
        }).toList());
        return dtoPage;
    }
}
