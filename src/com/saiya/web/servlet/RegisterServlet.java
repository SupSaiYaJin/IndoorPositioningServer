package com.saiya.web.servlet;

import com.saiya.dao.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理注册请求的Servlet
 * 需要客户端发起请求方式为POST,在Http头中加入username参数,代表用户名,加入password参数,代表密码
 * 返回JSON数据,格式为{"registerResult":注册信息码}
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        int registerResult = databaseManager.register(username, password);

        resp.setContentType("application/json");
        String responseJSON = String.format("{\"registerResult\":%d}", registerResult);
        PrintWriter out = resp.getWriter();
        out.print(responseJSON);
        out.flush();
    }
}
