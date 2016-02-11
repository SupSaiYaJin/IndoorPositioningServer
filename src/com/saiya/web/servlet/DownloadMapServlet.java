package com.saiya.web.servlet;

import com.saiya.dao.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 处理下载地图请求的Servlet
 * 需要客户端发起请求方式为POST,在Http头中加入sceneName参数,表示地图名称
 * 返回地图文件,Http头中附加fileName参数,表示文件名
 */
@WebServlet(name = "DownloadMapServlet", urlPatterns = "/positioning/downloadmap")
public class DownloadMapServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String sceneName = req.getParameter("sceneName");
        byte[] mapBytes = databaseManager.getSceneMap(sceneName);

        resp.setCharacterEncoding("utf-8");
        resp.setHeader("Content-Disposition","attachment; filename=\"" + sceneName + ".jpg\"");
        resp.setContentType("image/jpeg");
        if(mapBytes == null) {
            resp.setContentLength(0);
            return;
        }
        resp.setContentLength(mapBytes.length);
        OutputStream out = resp.getOutputStream();
        out.write(mapBytes);
        out.flush();
    }
}