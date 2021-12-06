package com.lgy.kill.server.controller;

import com.lgy.kill.api.enums.StatusCode;
import com.lgy.kill.api.response.BaseResponse;
import com.lgy.kill.model.dto.KillSuccessUserInfo;
import com.lgy.kill.model.mapper.ItemKillSuccessMapper;
import com.lgy.kill.server.dto.KillDto;
import com.lgy.kill.server.service.IKillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class KillController {
    private static final Logger log = LoggerFactory.getLogger(KillController.class);
    private static final String prefix = "kill";

    @Autowired
    private IKillService killService;
    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 商品秒杀核心业务逻辑
     * @param dto
     * @param result
     * @param session
     * @return
     */
    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session){
        if(result.hasErrors()||dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            Boolean res = killService.killItem(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }

        }catch(Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

    /**
     * 抢购成功跳转页面
     * @return
     */
    @GetMapping(value = prefix+"/execute/success")
    public String executeSuccess(){
        return "executeSuccess";
    }

    /**
     * 抢购失败跳转页面
     * @return
     */
    @GetMapping(value = prefix+"/execute/fail")
    public String executeFail(){
        return "executeFail";
    }

    /**
     * 邮箱上的通知连接
     * @param orderNo
     * @param modelMap
     * @return
     */
    @GetMapping(value = prefix + "/record/detail/{orderNo}")
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap){
        if(orderNo.isBlank()) {
            return "error";
        }
        KillSuccessUserInfo killSuccessUserInfo = itemKillSuccessMapper.selectByCode(orderNo);
        if(killSuccessUserInfo == null){
            return "error";

        }
        modelMap.put("info",killSuccessUserInfo);
        return "killRecord";
    }

    /**
     * 商品秒杀核心逻辑----用于压力测试
     * @param dto
     * @param result
     * @param session
     * @return
     */
    @RequestMapping(value = prefix+"/execute/lock",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BaseResponse executeLock(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session){
        if(result.hasErrors()||dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
//            不加分布式锁
//            Boolean res = killService.killItemV2(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"不加分布式锁----哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
//            Redis
//            Boolean res = killService.killItemV3(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"Redis分布式锁----哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
//            Redission
//            Boolean res = killService.killItemV4(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"Redisson分布式锁----哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
//          Zookeeper
            Boolean res = killService.killItemV5(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"Zookeeper分布式锁----哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        }catch(Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }
}
