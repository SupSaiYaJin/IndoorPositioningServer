package com.saiya.web.servlet;

import com.saiya.dao.DatabaseManager;
import com.saiya.service.location.SceneInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 处理得到数据库所有场景名称的Servlet
 * 需要客户端发起请求方式为POST
 * 返回JSON数据,包含场景名称与最后更新时间,格式为
 * {"maps":[{"sceneName":"name","scale":122,"lastUpdateTime":20151210},...]}
 */
@WebServlet(name = "GetSceneListServlet", urlPatterns = "/positioning/getscenelist")
public class GetSceneListServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        List<SceneInfo> sceneMap = databaseManager.getSceneList();
        resp.setContentType("application/json; charset=utf-8");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"maps\":[");
        for(SceneInfo info : sceneMap) {
            stringBuilder.append("{\"sceneName\":");
            stringBuilder.append(String.format("\"%s\",", info.getSceneName()));
            stringBuilder.append("\"scale\":");
            stringBuilder.append(info.getScale()).append(",");
            stringBuilder.append("\"lastUpdateTime\":");
            stringBuilder.append(info.getLastUpdateTime());
            stringBuilder.append("},");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]}");
        PrintWriter out = resp.getWriter();
        out.print(stringBuilder.toString());
        out.flush();
    }
}
