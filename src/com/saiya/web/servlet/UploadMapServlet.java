package com.saiya.web.servlet;

import com.saiya.dao.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;

/**
 * 处理更新地图请求的Servlet
 * 需要客户端发起请求方式为POST在Http头中加入sceneName参数,代表场景名称
 * 加入scale参数,代表比例尺,加入Part文件域sceneMap,代表地图文件
 * 返回JSON数据,格式为{"updateSucceed":是否成功}
 */
@WebServlet(name = "UploadMapServlet", urlPatterns = "/positioning/uploadmap")
@MultipartConfig
public class UploadMapServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String sceneName = URLDecoder.decode(req.getHeader("sceneName"), "utf-8");
        float scale = Float.parseFloat(req.getHeader("scale"));
        Part part = req.getPart("sceneMap");
        byte[] mapBytes = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int bufSize = 1024;
            byte[] buffer = new byte[bufSize];
            int len;
            InputStream in = part.getInputStream();
            while ((len = in.read(buffer, 0, bufSize)) != -1) {
                out.write(buffer, 0, len);
            }
            mapBytes = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean updateSucceed = databaseManager.updateSceneMap(sceneName, scale, mapBytes);

        resp.setContentType("application/json");
        String responseJSON = String.format("{\"uploadSucceed\":%b}", updateSucceed);
        PrintWriter out = resp.getWriter();
        out.write(responseJSON);
        out.flush();
    }
}
