package com.lwq.controller;

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

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){
        log.info("hello");
        return "hello,permission";
    }
}
