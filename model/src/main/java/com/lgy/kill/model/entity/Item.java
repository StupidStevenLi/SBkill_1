package com.lgy.kill.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Item {
    /**
     * 商品id主键
     */
    private Integer id;

    /**
     * 商品名
     */
    private String name;

    /**
     * 商品编号
     */
    private String code;

    /**
     * 库存
     */
    private Long stock;

    /**
     * 采购时间
     */
    private Date purchaseTime;

    /**
     * 是否有效（1=是；0=否）
     */
    private Integer isActive;

    /**
     *
     */
    private Date createTime;

    /**
     * '更新时间
     */
    private Date updateTime;

}