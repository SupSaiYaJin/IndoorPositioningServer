package com.saiya.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理用户注销请求的Servlet
 * 需要客户端发起请求方式为GET
 * 返回JSON数据,格式为{"logoutResult":注销信息码}
 */

@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //如果Session存在,则销毁它
        HttpSession session = req.getSession(false);
        if(session != null) {
            session.invalidate();
        }

        resp.setContentType("application/json");
        String responseJSON = "{\"loginResult\":true}";
        PrintWriter out = resp.getWriter();
        out.print(responseJSON);
        out.flush();
    }
}
