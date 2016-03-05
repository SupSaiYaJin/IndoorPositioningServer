package com.saiya.web.servlet;

import com.saiya.dao.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理登录请求的Servlet
 * 需要客户端发起请求方式为POST,在Http头中加入username参数,代表用户名,加入password参数,代表密码
 * 返回JSON数据,格式为{"loginResult":登录信息码}
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet{

    /** 最大未激活时间,超过此时间后Session失效 */
    private static final int MAX_INACTIVE_INTERVAL = 60 * 30;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        int loginResult = databaseManager.login(username, password);

        /** 若登录成功,在Session中加入参数username,并设置最大未激活时间 */
        if (loginResult == DatabaseManager.LOGIN_SUCCEED) {
            HttpSession session = req.getSession();
            session.setAttribute("username", username);
            session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
        }

        resp.setContentType("application/json");
        String responseJSON = String.format("{\"loginResult\":%d}", loginResult);
        PrintWriter out = resp.getWriter();
        out.print(responseJSON);
        out.flush();
    }
}
