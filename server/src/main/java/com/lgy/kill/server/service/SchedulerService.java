package com.lgy.kill.server.service;

import com.lgy.kill.model.entity.ItemKillSuccess;
import com.lgy.kill.model.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {
    private Logger log = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private Environment env;

    /**
     * 定时获取status=0的订单并判断是否超过TTL，然后进行失效
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void schedulerExpireOrders(){
        log.info("v1的定时任务----");
        try{
            List<ItemKillSuccess> list = itemKillSuccessMapper.selectExpireOrders();
            if(list != null && !list.isEmpty()){
                for(ItemKillSuccess e : list){
                    if(e!=null && e.getDiffTime()>env.getProperty("scheduler.expire.orders.time",Integer.class)){
                        itemKillSuccessMapper.expireOrder(e.getCode());

                    }

                }
            }
        }catch(Exception e){
            log.error("定时获取status=0的订单并判断是否超过TTL，然后进行失效-发生异常：",e.fillInStackTrace());
        }
    }

    @Scheduled(cron = "0/21 * * * * ?")
    public void schedulerExpireOrdersV2(){
        log.info("v2的定时任务----");
    }

    @Scheduled(cron = "0/22 * * * * ?")
    public void schedulerExpireOrdersV3(){
        log.info("v3的定时任务----");
    }
}
