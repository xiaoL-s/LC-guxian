package com.guxian.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.system.dto.SysOperLogDTO;
import com.guxian.mybatis.entity.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {
    IPage<SysOperLogDTO> page(IPage<SysOperLog> page, String operModule, String realName, String startTime, String endTime);
}
