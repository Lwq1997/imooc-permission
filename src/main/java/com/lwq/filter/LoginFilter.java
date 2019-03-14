package com.lwq.filter;

import com.lwq.common.RequestHolder;
import com.lwq.model.SysUser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Lwq
 * @Date: 2019/3/14 17:19
 * @Version 1.0
 * @Describe
 */
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        SysUser sysUser = (SysUser) req.getSession().getAttribute("user");

        if(sysUser==null){
            String path = "/signin.jsp";
            resp.sendRedirect(path);
            return;
        }

        RequestHolder.add(sysUser);
        RequestHolder.add(req);

        filterChain.doFilter(servletRequest,servletResponse);

        return;
    }

    @Override
    public void destroy() {

    }
}
