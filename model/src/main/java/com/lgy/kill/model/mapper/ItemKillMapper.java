package com.lgy.kill.model.mapper;

import com.lgy.kill.model.entity.ItemKill;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

public interface ItemKillMapper {
    List<ItemKill> selectAll();

    ItemKill selectById(@Param("id") Integer id);

    int updateKillItem(@Param("killId") Integer killId);

    ItemKill selectByIdV2(@Param("id") Integer id);

    int updateKillItemV2(@Param("killId") Integer killId);
}