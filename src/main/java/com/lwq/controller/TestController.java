package com.lwq.controller;

import com.lwq.common.ApplicationContextHelper;
import com.lwq.common.JsonData;
import com.lwq.dao.SysAclModuleMapper;
import com.lwq.exception.ParamException;
import com.lwq.exception.PermissionException;
import com.lwq.model.SysAclModule;
import com.lwq.util.BeanValidator;
import com.lwq.util.JsonMapper;
import com.param.TestVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 1:18
 * @Version 1.0
 * @Describe
 */
@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {

    @RequestMapping("/hello01")
    @ResponseBody
    public String hello01(){
        log.info("hello01");
        return "hello01,permission";
    }

    @RequestMapping("/hello02.json")
    @ResponseBody
    public JsonData hello02(){
        log.info("hello02");
        return JsonData.success("hello02,permission");
    }

    @RequestMapping("/hello03.json")
    @ResponseBody
    public JsonData hello03(){
        log.info("hello03");
        throw new PermissionException("test hello03 exception");
//        return JsonData.success("hello03,permission");
    }

    @RequestMapping("/hello04.json")
    @ResponseBody
    public JsonData hello04(){
        log.info("hello04");
        throw new RuntimeException("test hello04 exception");
    }

    @RequestMapping("/validate01.json")
    @ResponseBody
    public JsonData validate01(TestVo vo){
        log.info("validate01");
        try {
            Map<String,String> map = BeanValidator.validateObject(vo);
            if(MapUtils.isNotEmpty(map)){
                for(Map.Entry<String,String> entry:map.entrySet()){
                    log.info("{}-->{}",entry.getKey(),entry.getValue());
                }
            }
        }catch (Exception e){

        }
        return JsonData.success("test validate01");
    }

    @RequestMapping("/validate02.json")
    @ResponseBody
    public JsonData validate02(TestVo vo)throws ParamException {
        log.info("validate02");
        BeanValidator.check(vo);
        return JsonData.success("test validate02");
    }

    @RequestMapping("/jsonAndApplication01.json")
    @ResponseBody
    public JsonData jsonAndApplication01()throws ParamException {
        log.info("jsonAndApplication01");
        SysAclModuleMapper sysAclModuleMapper = ApplicationContextHelper.popBean(SysAclModuleMapper.class);
        SysAclModule module = sysAclModuleMapper.selectByPrimaryKey(1);
        log.info(JsonMapper.object2String(module));
        return JsonData.success("test jsonAndApplication01");
    }
}
