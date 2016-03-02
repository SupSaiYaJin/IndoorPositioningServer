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
 * 处理更新指纹库请求的Servlet
 * 需要客户端发起请求方式为POST
 * 在Http头中
 *    加入updateType参数,代表更新类型
 *    加入sceneName参数,代表场景名称
 *    加入location_x参数,代表更新位置的X坐标
 *    加入location_y参数,代表更新位置的Y坐标
 * 若更新Wifi指纹库
 *    加入mac参数,代表更新位置所有MAC地址
 *    加入rssi参数,代表更新位置所有RSSI
 * 若更新地磁数据库
 *    加入geomagnetic_y,代表更新位置的Y方向磁场强度
 *    加入geomagnetic_z,代表更新位置的Z方向磁场强度
 * 返回JSON数据,格式为{"updateSucceed":是否成功}
 */
@WebServlet(name = "UpdateDataServlet", urlPatterns = "/positioning/updatedata")
public class UpdateDataServlet extends HttpServlet {

    /** 更新WiFi指纹 */
    public static final String UPDATE_WIFI = "wifi";

    /** 更新地磁指纹 */
    public static final String UPDATE_GEOMAGNETIC = "geomagnetic";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        boolean updateSucceed = false;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        req.setCharacterEncoding("utf-8");
        String updateType = req.getParameter("updateType");
        String sceneName = req.getParameter("sceneName");
        double location_x = Double.parseDouble(req.getParameter("location_x"));
        double location_y = Double.parseDouble(req.getParameter("location_y"));

        //更新Wifi指纹
        if (updateType.equals(UPDATE_WIFI)) {
            String mac = req.getParameter("mac");
            String rssi = req.getParameter("rssi");
            updateSucceed = databaseManager.updateWifiFingerPrint
                    (sceneName, location_x, location_y, mac, rssi);
        }

        //更新地磁指纹
        if (updateType.equals(UPDATE_GEOMAGNETIC)){
            double geomagnetic_y = Double.parseDouble(req.getParameter("geomagnetic_y"));
            double geomagnetic_z = Double.parseDouble(req.getParameter("geomagnetic_z"));
            updateSucceed = databaseManager.updateGeoFingerprint
                    (sceneName, location_x, location_y, geomagnetic_y, geomagnetic_z);
        }

        resp.setContentType("application/json");
        String responseJSON = String.format("{\"updateSucceed\":%b}", updateSucceed);
        PrintWriter out = resp.getWriter();
        out.write(responseJSON);
        out.flush();
    }
}
