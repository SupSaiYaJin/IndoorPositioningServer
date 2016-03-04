package com.saiya.dao;

import com.saiya.dao.hbm.GeoFingerprint;
import com.saiya.dao.hbm.SceneInfo;
import com.saiya.dao.hbm.User;
import com.saiya.dao.hbm.WifiFingerprint;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责所有与数据库有关的操作
 */
public class DatabaseManager {

    /**
     * 未知错误
     */
    public static final int UNEXPECTED_ERROR = -1;

    /**
     * 登录成功
     */
    public static final int LOGIN_SUCCEED = 0;

    /**
     * 用户名不存在
     */
    public static final int USERNAME_NOT_EXIST = 1;

    /**
     * 密码错误
     */
    public static final int PASSWORD_ERROR = 2;

    /**
     * 注册成功
     */
    public static final int REGISTER_SUCCEED = 3;

    /**
     * 用户名重复
     */
    public static final int DUPLICATE_USERNAME = 4;

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public DatabaseManager() {}

    private static DatabaseManager databaseManager;

    /**
     * 缓存WiFi位置指纹
     */
    private static Map<String, List<WifiFingerprint>> wifiFingerprintCache;

    /**
     * 缓存地磁位置指纹
     */
    private static Map<String, List<GeoFingerprint>> geoFingerprintCache;

    /**
     * 缓存场景ID
     */
    private static Map<String, Integer> sceneIdCache;

    /**
     * 缓存场景信息列表
     */
    private static List<SceneInfo> sceneInfoCache;

    static {
        wifiFingerprintCache = new HashMap<>();
        geoFingerprintCache = new HashMap<>();
        sceneIdCache = new HashMap<>();
        sceneInfoCache = new ArrayList<>();
        ApplicationContext applicationContext
                = new ClassPathXmlApplicationContext("applicationContext.xml");
        databaseManager = (DatabaseManager) applicationContext.getBean("databaseManager");
    }

    public static DatabaseManager getInstance() {
        return databaseManager;
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录信息码
     */
    @Transactional(readOnly = true)
    public int login(String username, String password) {
        try {
            Session session = sessionFactory.getCurrentSession();
            User user = (User) session.createQuery("from User where username = ?")
                    .setString(0, username).uniqueResult();
            if (user == null) {
                return USERNAME_NOT_EXIST;
            } else {
                if (user.getPassword().equals(password)) {
                    return LOGIN_SUCCEED;
                } else {
                    return PASSWORD_ERROR;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return UNEXPECTED_ERROR;
        }
    }

    /**
     * 注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册信息码
     */
    @Transactional
    public int register(String username, String password) {
        try {
            Session session = sessionFactory.getCurrentSession();
            User user = (User) session.createQuery("from User where username = ?")
                    .setString(0, username).uniqueResult();
            if (user != null) {
                return DUPLICATE_USERNAME;
            } else {
                user = new User();
                user.setUsername(username);
                user.setPassword(password);
                session.save(user);
                return REGISTER_SUCCEED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return UNEXPECTED_ERROR;
        }
    }

    /**
     * 更新WiFi指纹信息
     *
     * @param scene_name 要更新的场景名称
     * @param location_x 要更新位置的X坐标
     * @param location_y 要更新位置的Y坐标
     * @param mac        要更新位置的MAC地址,形式为mac1,mac2...,macN
     * @param rssi       要更新位置的RSSI,形式为rssi1,rssi2...,rssiN
     * @return 返回true为更新成功, false为失败
     */
    @Transactional
    public boolean updateWifiFingerPrint(String scene_name, double location_x, double location_y,
            String mac, String rssi) {
        boolean isExisted = true;
        Session session = sessionFactory.getCurrentSession();
        int scene_id = getSceneId(scene_name);
        if (scene_id == -1) {
            return false;
        } else {
            WifiFingerprint wifiFingerprint =
                    (WifiFingerprint) session.createQuery("from WifiFingerprint where sceneId = ? " +
                    "and ABS(locationX - ?) < 1e-5 AND ABS(locationY - ?) < 1e-5")
                    .setInteger(0, scene_id)
                    .setDouble(1, location_x)
                    .setDouble(2, location_y).uniqueResult();
            if (wifiFingerprint == null) {
                wifiFingerprint = new WifiFingerprint();
                isExisted = false;
            }
            wifiFingerprint.setSceneId(scene_id);
            wifiFingerprint.setLocationX(location_x);
            wifiFingerprint.setLocationY(location_y);
            wifiFingerprint.setMac(mac);
            wifiFingerprint.setRssi(rssi);
            if (!isExisted) {
                session.save(wifiFingerprint);
            } else {
                session.update(wifiFingerprint);
            }
        }
        wifiFingerprintCache.remove(scene_name);
        return true;
    }

    /**
     * 更新地磁指纹数据
     *
     * @param scene_name    要更新的场景名称
     * @param location_x    要更新位置的X坐标
     * @param location_y    要更新位置的Y坐标
     * @param geomagnetic_y 要更新位置的Y方向的磁场强度
     * @param geomagnetic_z 要更新位置的Z方向的磁场强度
     * @return 返回true为更新成功, false为失败
     */
    @Transactional
    public boolean updateGeoFingerprint(String scene_name, double location_x, double location_y,
            double geomagnetic_y, double geomagnetic_z) {
        boolean isExisted = true;
        Session session = sessionFactory.getCurrentSession();
        int scene_id = getSceneId(scene_name);
        if (scene_id == -1) {
            return false;
        } else {
            GeoFingerprint geoFingerprint = (GeoFingerprint) session
                    .createQuery("from GeoFingerprint where sceneId = ? " +
                            "and ABS(locationX - ?) < 1e-5 AND ABS(locationY - ?) < 1e-5")
                            .setInteger(0, scene_id)
                            .setDouble(1, location_x)
                            .setDouble(2, location_y).uniqueResult();
            if (geoFingerprint == null) {
                geoFingerprint = new GeoFingerprint();
                isExisted = false;
            }
            geoFingerprint.setSceneId(scene_id);
            geoFingerprint.setLocationX(location_x);
            geoFingerprint.setLocationY(location_y);
            geoFingerprint.setGeomagneticY(geomagnetic_y);
            geoFingerprint.setGeomagneticZ(geomagnetic_z);
            if (!isExisted) {
                session.save(geoFingerprint);
            } else {
                session.update(geoFingerprint);
            }
        }
        geoFingerprintCache.remove(scene_name);
        return true;
    }

    /**
     * 更新场景地图
     *
     * @param scene_name 要更新的场景名称
     * @param scale      场景比例尺,含义为多少个像素对应1米
     * @param mapBytes   地图文件的字节数组
     * @return 返回true为更新成功, false为更新失败
     */
    @Transactional
    public boolean updateSceneMap(String scene_name, double scale, byte[] mapBytes) {
        boolean isExisted = true;
        Session session = sessionFactory.getCurrentSession();
        SceneInfo sceneInfo = (SceneInfo) session
                .createQuery("select new SceneInfo(id, sceneName, scale)from SceneInfo where sceneName = ?")
                .setString(0, scene_name).uniqueResult();
        if (sceneInfo == null) {
            sceneInfo = new SceneInfo();
            isExisted = false;
        }
        sceneInfo.setSceneName(scene_name);
        if (scale != 0) {
            sceneInfo.setScale(scale);
        }
        sceneInfo.setSceneMap(mapBytes);
        if (!isExisted) {
            session.save(sceneInfo);
        } else {
            session.update(sceneInfo);
        }
        sceneInfoCache.clear();
        return true;
    }

    /**
     * 得到对应场景的所有WiFi指纹
     *
     * @param scene_name 场景名称
     * @return 返回由WifiFingerprint对象组成的List, 每个WifiFingerprint对应一个位置的WiFi位置指纹
     */
    @Transactional(readOnly = true)
    public List<WifiFingerprint> getWifiFingerprint(String scene_name) {
        if (wifiFingerprintCache.containsKey(scene_name)) {
            return wifiFingerprintCache.get(scene_name);
        }
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<WifiFingerprint> result = session
                .createQuery("from WifiFingerprint where sceneId = ?")
                .setInteger(0, getSceneId(scene_name))
                .list();
        wifiFingerprintCache.put(scene_name, result);
        return result;
    }

    /**
     * 得到对应场景的所有地磁指纹
     *
     * @param scene_name 场景名称
     * @return 返回由GeoFingerprint对象组成的List, 每个GeoFingerprint对应一个位置的地磁位置指纹
     */
    @Transactional(readOnly = true)
    public List<GeoFingerprint> getGeoFingerprint(String scene_name) {
        if (geoFingerprintCache.containsKey(scene_name)) {
            return geoFingerprintCache.get(scene_name);
        }
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<GeoFingerprint> result = session
                .createQuery("from GeoFingerprint where sceneId = ?")
                .setInteger(0, getSceneId(scene_name))
                .list();
        geoFingerprintCache.put(scene_name, result);
        return result;
    }

    /**
     * 得到数据库中所有场景的名称以及最后更新时间
     *
     * @return 返回一个List存储SceneInfo对象, 包含场景名称, 比例尺, 最后更新时间的信息
     */
    @Transactional(readOnly = true)
    public List<SceneInfo> getSceneList() {
        if (sceneInfoCache.size() != 0) {
            return sceneInfoCache;
        }
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<SceneInfo> result = session.createQuery
                ("select new SceneInfo(id, sceneName, scale, lastUpdateTime) from SceneInfo ")
                .list();
        sceneInfoCache = result;
        return result;
    }

    /**
     * 根据场景名得到场景ID
     *
     * @param scene_name 场景名称
     * @return 返回场景ID
     */
    @Transactional(readOnly = true)
    public int getSceneId(String scene_name) {
        if (sceneIdCache.containsKey(scene_name)) {
            return sceneIdCache.get(scene_name);
        }
        Session session = sessionFactory.getCurrentSession();
        SceneInfo sceneInfo = (SceneInfo) session.createQuery("from SceneInfo where sceneName = ?")
                .setString(0, scene_name).uniqueResult();
        if (sceneInfo != null) {
            sceneIdCache.put(scene_name, sceneInfo.getId());
            return sceneInfo.getId();
        } else {
            return -1;
        }
    }

    /**
     * 根据场景名得到场景地图的字节数组
     *
     * @param scene_name 场景名称
     * @return 返回场景对应地图的字节数组, 若场景地图不存在, 返回null
     */
    @Transactional(readOnly = true)
    public byte[] getSceneMap(String scene_name) {
        Session session = sessionFactory.getCurrentSession();
        return (byte[]) session
                .createQuery("select sceneMap from SceneInfo where sceneName = ?")
                .setString(0, scene_name)
                .uniqueResult();
    }
}
