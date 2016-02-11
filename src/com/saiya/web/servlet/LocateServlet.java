package com.saiya.web.servlet;

import com.saiya.service.location.Algorithms;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理定位请求的Servlet
 * 需要客户端发起请求方式为POST
 * 在Http头中
 *    加入locateType参数,代表定位方式
 *    加入sceneName参数,代表场景名称
 * 若采用WiFi定位方式
 *    加入mac参数,代表待测位置所有MAC地址
 *    加入rssi参数,代表待测位置所有RSSI
 * 若采用地磁定位方式
 *    加入geomagnetic_y,代表待测位置的Y方向磁场强度
 *    加入geomagnetic_z,代表待测位置的Z方向磁场强度
 * 返回JSON数据,格式为{"result_x":X坐标,"result_y",Y坐标}
 */
@WebServlet(name = "LocateServlet", urlPatterns = "/positioning/locate")
public class LocateServlet extends HttpServlet {
    /** 采用WiFi和地磁混合定位方式 */
    public static final int USE_ALL_METHOD = 0;

    /** 采用WiFi定位方式 */
    public static final int USE_WIFI_ONLY = 1;

    /** 采用地磁定位方式 */
    public static final int USE_GEOMAGNETIC_ONLY = 2;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        String sceneName = req.getParameter("sceneName");
        int locateType = Integer.parseInt(req.getParameter("locateType"));
        String mac;
        String rssi;
        float geomagnetic_y;
        float geomagnetic_z;
        float[] result;
        switch (locateType) {
            case USE_ALL_METHOD:
                mac = req.getParameter("mac");
                rssi = req.getParameter("rssi");
                geomagnetic_y = Float.parseFloat(req.getParameter("geomagnetic_y"));
                geomagnetic_z = Float.parseFloat(req.getParameter("geomagnetic_z"));
                result = Algorithms.locateOnAll(sceneName, mac, rssi,
                        geomagnetic_y, geomagnetic_z);
                break;
            case USE_WIFI_ONLY:
                mac = req.getParameter("mac");
                rssi = req.getParameter("rssi");
                result = Algorithms.locateOnWifi(sceneName, mac, rssi);
                break;
            case USE_GEOMAGNETIC_ONLY:
                geomagnetic_y = Float.parseFloat(req.getParameter("geomagnetic_y"));
                geomagnetic_z = Float.parseFloat(req.getParameter("geomagnetic_z"));
                result = Algorithms.locateOnGeomagnetic(sceneName, geomagnetic_y, geomagnetic_z);
                break;
            default:
                result = new float[2];
                break;
        }

        resp.setContentType("application/json");
        String responseJSON = String.format
                ("{\"result_x\":%f, \"result_y\":%f}", result[0], result[1]);
        PrintWriter out = resp.getWriter();
        out.print(responseJSON);
        out.flush();
    }
}
