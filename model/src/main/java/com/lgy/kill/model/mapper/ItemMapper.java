package com.lgy.kill.model.mapper;

import com.lgy.kill.model.entity.Item;

public interface ItemMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Item record);

    int insertSelective(Item record);

    Item selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Item record);

    int updateByPrimaryKey(Item record);
}