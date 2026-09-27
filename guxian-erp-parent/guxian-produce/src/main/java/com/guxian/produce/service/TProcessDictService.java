package com.guxian.produce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.produce.entity.TProcessDict;

import java.util.List;

public interface TProcessDictService extends IService<TProcessDict> {

    List<TProcessDict> listEnabled();

    void saveProcess(TProcessDict process);
}
