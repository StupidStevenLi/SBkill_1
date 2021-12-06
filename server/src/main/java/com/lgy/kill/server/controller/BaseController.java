package com.lgy.kill.server.controller;

import com.lgy.kill.api.enums.StatusCode;
import com.lgy.kill.api.response.BaseResponse;
import com.mysql.cj.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("base")
public class BaseController {


    /**
     * 跳转页面
     * @param name
     * @param modelMap
     * @return
     */
    @GetMapping("/welcome")
    public String welcome(String name , ModelMap modelMap){
        if(StringUtils.isEmptyOrWhitespaceOnly(name)){
            name = "这是跳转modelWelcom";
        }
        modelMap.put("name",name);
        return "welcom";
    }

    /**
     * 前端发起请求获取数据
     * @param name
     * @return
     */
    @GetMapping("/data")
    @ResponseBody
    public String data(String name){
        if(StringUtils.isEmptyOrWhitespaceOnly(name)){
            name = "这是前端发起请求获取数据dataWelcom";
        }
        return name;
    }

    /**
     * 标准请求响应数据格式
     * @param name
     * @return
     */
    @RequestMapping(value = "/response",method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse response(String name){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        if (StringUtils.isEmptyOrWhitespaceOnly(name)){
            name="这是请求响应数据格式welcome!";
        }
        response.setData(name);
        return response;
    }

    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String error(){
        return "error";
    }

}
