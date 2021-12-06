package com.lgy.kill.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Data
@Getter
@Setter
@ToString
public class ItemKill {
    private Integer id;

    private Integer item_id;

    private Integer total;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date start_time;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date end_time;

    private Byte is_active;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date create_time;

    private String itemName;

    //采用服务器时间控制是否可以进行抢购
    private Integer canKill;
}