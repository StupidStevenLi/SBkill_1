package com.lgy.kill.server.service;

import com.lgy.kill.model.entity.ItemKill;

import java.util.List;

public interface IItemService {

    List<ItemKill> getKillItems() throws Exception;
    ItemKill getKillDetail(Integer id) throws Exception;
}
