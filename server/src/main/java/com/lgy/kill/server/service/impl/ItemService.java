package com.lgy.kill.server.service.impl;

import com.lgy.kill.model.entity.ItemKill;
import com.lgy.kill.model.mapper.ItemKillMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ItemService implements com.lgy.kill.server.service.IItemService {
    //private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    private ItemKillMapper itemKillMapper;

    /**
     * 获取待秒杀商品列表
     * @return
     * @throws Exception
     */
    @Override
    public List<ItemKill> getKillItems() throws Exception {
        return itemKillMapper.selectAll();
    }

    /**
     * 获取待秒杀商品详情
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill itemKill = itemKillMapper.selectById(id);
        if(itemKill==null){
            throw new Exception("获取秒杀商品详情-待秒杀商品记录不存在");
        }
        return itemKill;
    }
}
