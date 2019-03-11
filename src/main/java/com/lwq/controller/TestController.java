package com.lwq.controller;

import com.lwq.common.JsonData;
import com.lwq.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
