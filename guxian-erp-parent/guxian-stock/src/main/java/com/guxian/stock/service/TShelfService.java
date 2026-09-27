package com.guxian.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.stock.entity.TShelf;

import java.util.List;

public interface TShelfService extends IService<TShelf> {

    List<TShelf> listEnabled();

    void saveShelf(TShelf shelf);
}
