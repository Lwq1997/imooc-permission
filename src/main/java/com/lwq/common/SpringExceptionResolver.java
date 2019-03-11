package com.lwq.common;

import com.lwq.exception.ParamException;
import com.lwq.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 17:56
 * @Version 1.0
 * @Describe  全局处理的异常类
 */
@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception ex) {
        String url = httpServletRequest.getRequestURL().toString();
        ModelAndView mv;
        String defaultMsg = "System error";

        //这里要求项目中所有请求json数据的，都用.json结尾
        if(url.endsWith(".json")){
            //用.json结尾
            if(ex instanceof PermissionException || ex instanceof ParamException){
                JsonData result = JsonData.fail(ex.getMessage());
                //<bean id="jsonView" class="org.springframework.web.servlet.view.json.MappingJackson2JsonView"/>
                mv = new ModelAndView("jsonView",result.toMap());
            }else {
                log.error("unknow json exception,url: "+url,ex);
                JsonData result = JsonData.fail(defaultMsg);
                mv = new ModelAndView("jsonView",result.toMap());
            }
        }else if(url.endsWith(".page")) {//这里要求项目中所有请求page页面的，都用.page结尾
            log.error("unknow page exception,url: "+url,ex);
            JsonData result = JsonData.fail(defaultMsg);
            //会找到exception.jsp界面
            mv = new ModelAndView("exception",result.toMap());
        }else {
            log.error("unknow exception,url: "+url,ex);
            JsonData result = JsonData.fail(defaultMsg);
            mv = new ModelAndView("jsonView",result.toMap());
        }

        return mv;
    }
}
