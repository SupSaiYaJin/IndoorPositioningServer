package com.saiya.web.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 鉴定对/positioning路径下发起请求的客户端是否已登录
 */
@WebFilter(urlPatterns = "/positioning/*")
public class AuthorityFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter
    (ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession();
        if(session.getAttribute("username") != null) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else {
            servletResponse.setContentType("application/json");
            PrintWriter out = servletResponse.getWriter();
            out.print("{\"authorized\":false}");
            out.flush();
        }
    }

    @Override
    public void destroy() {

    }
}
