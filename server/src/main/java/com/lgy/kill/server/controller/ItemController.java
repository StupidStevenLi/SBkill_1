package com.lgy.kill.server.controller;

import com.lgy.kill.model.entity.ItemKill;
import com.lgy.kill.server.service.IItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 待秒杀商品Controoler
 */
@Controller
public class ItemController {
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private static final String prefix = "item";

    @Autowired
    private IItemService itemService;

    /**
     * 获取商品列表
     * @return
     */
    @RequestMapping(value = {"/","/index",prefix+"/list",prefix+"/index.html"},method = RequestMethod.GET)
    public String list(ModelMap modelMap){
        try {
            //获取待秒杀商品列表
            List<ItemKill> list=itemService.getKillItems();
            modelMap.put("list",list);
            modelMap.put("list1","nima");
            log.info("获取待秒杀商品列表-数据：{}",list);
        }catch (Exception e){
            log.error("获取待秒杀商品列表-发生异常：",e.fillInStackTrace());
            return "redirect:/base/error";
        }
        return "list";
    }

    @GetMapping(value = prefix+"/detail/{id}")
    public String detail(@PathVariable Integer id,ModelMap modelMap){
        if(id==null||id<0){
            return "redirect:/base/error";
        }
        try {
            ItemKill itemKill=itemService.getKillDetail(id);
            modelMap.put("detail",itemKill);
        }catch (Exception e){
            log.error("获取待秒杀商品的详情发生异常：id={}",id,e.fillInStackTrace());
            return "redirect:/base/error";
        }
        return "info";
    }



}
