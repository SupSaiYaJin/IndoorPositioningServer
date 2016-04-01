package com.saiya.web.servlet;

import com.saiya.dao.hbm.SceneInfo;
import com.saiya.service.location.Algorithms;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理找到终端所在场景请求的Servlet
 * 需要客户端发起请求方式为POST
 * 在Http头中
 *    加入mac参数,代表待测位置所有MAC地址
 * 返回JSON数据,格式为{"scene_name":场景名}
 */
@WebServlet(name = "LocateSceneServlet", urlPatterns = "/positioning/locatescene")
public class LocateSceneServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        String mac = req.getParameter("mac");
        SceneInfo result = Algorithms.locateScene(mac);

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        String responseJSON =
                String.format("{\"sceneName\":\"%s\",\"scale\":%f,\"lastUpdateTime\":%d}",
                result.getSceneName(), result.getScale(), result.getLastUpdateTime().getTime());
        PrintWriter out = resp.getWriter();
        out.print(responseJSON);
        out.flush();
    }
}
