package com.saiya.dao;

import com.saiya.dao.hbm.GeoFingerprint;
import com.saiya.dao.hbm.SceneInfo;
import com.saiya.dao.hbm.User;
import com.saiya.dao.hbm.WifiFingerprint;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    private static DatabaseManager mDatabaseManager;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        return mDatabaseManager;
    }

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
        mDatabaseManager = new DatabaseManager();
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录信息码
     */
    public int login(String username, String password) {
        int responseCode;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        User user = (User) session.createQuery("from User where username = ?")
                .setString(0, username).uniqueResult();
        if (user == null) {
            responseCode = USERNAME_NOT_EXIST;
        } else {
            if (user.getPassword().equals(password)) {
                responseCode = LOGIN_SUCCEED;
            } else {
                responseCode = PASSWORD_ERROR;
            }
        }
        tx.commit();
        session.close();
        return responseCode;
    }

    /**
     * 注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册信息码
     */
    public int register(String username, String password) {
        int responseCode = UNEXPECTED_ERROR;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            User user = (User) session.createQuery("from User where username = ?")
                    .setString(0, username).uniqueResult();
            if (user != null) {
                responseCode = DUPLICATE_USERNAME;
            } else {
                user = new User();
                user.setUsername(username);
                user.setPassword(password);
                session.save(user);
                responseCode = REGISTER_SUCCEED;
            }
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            responseCode = UNEXPECTED_ERROR;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return responseCode;
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
    public boolean updateWifiFingerPrint(String scene_name, double location_x, double location_y,
            String mac, String rssi) {
        boolean isSucceed = false;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                isSucceed = false;
            } else {
                WifiFingerprint wifiFingerprint =
                        (WifiFingerprint) session.createQuery("from WifiFingerprint where sceneId = ? " +
                        "and ABS(locationX - ?) < 1e-5 AND ABS(locationY - ?) < 1e-5")
                        .setInteger(0, scene_id)
                        .setDouble(1, location_x)
                        .setDouble(2, location_y).uniqueResult();
                if (wifiFingerprint == null) {
                    wifiFingerprint = new WifiFingerprint();
                }
                wifiFingerprint.setSceneId(scene_id);
                wifiFingerprint.setLocationX(location_x);
                wifiFingerprint.setLocationY(location_y);
                wifiFingerprint.setMac(mac);
                wifiFingerprint.setRssi(rssi);
                session.saveOrUpdate(wifiFingerprint);
            }
            isSucceed = true;
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            isSucceed = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        wifiFingerprintCache.remove(scene_name);
        return isSucceed;
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
    public boolean updateGeoFingerprint(String scene_name, double location_x, double location_y,
            double geomagnetic_y, double geomagnetic_z) {
        boolean isSucceed = false;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                isSucceed = false;
            } else {
                GeoFingerprint geoFingerprint = (GeoFingerprint) session
                        .createQuery("from GeoFingerprint where sceneId = ? " +
                                "and ABS(locationX - ?) < 1e-5 AND ABS(locationY - ?) < 1e-5")
                                .setInteger(0, scene_id)
                                .setDouble(1, location_x)
                                .setDouble(2, location_y).uniqueResult();
                if (geoFingerprint == null) {
                    geoFingerprint = new GeoFingerprint();
                }
                geoFingerprint.setSceneId(scene_id);
                geoFingerprint.setLocationX(location_x);
                geoFingerprint.setLocationY(location_y);
                geoFingerprint.setGeomagneticY(geomagnetic_y);
                geoFingerprint.setGeomagneticZ(geomagnetic_z);
                session.saveOrUpdate(geoFingerprint);
            }
            isSucceed = true;
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            isSucceed = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        geoFingerprintCache.remove(scene_name);
        return isSucceed;
    }

    /**
     * 更新场景地图
     *
     * @param scene_name 要更新的场景名称
     * @param scale      场景比例尺,含义为多少个像素对应1米
     * @param mapBytes   地图文件的字节数组
     * @return 返回true为更新成功, false为更新失败
     */
    public boolean updateSceneMap(String scene_name, double scale, byte[] mapBytes) {
        boolean isSucceed = false;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            SceneInfo sceneInfo = (SceneInfo) session
                    .createQuery("select new SceneInfo(sceneName, scale)from SceneInfo where sceneName = ?")
                    .setString(0, scene_name).uniqueResult();
            if (sceneInfo == null) {
                sceneInfo = new SceneInfo();
            }
            sceneInfo.setSceneName(scene_name);
            sceneInfo.setScale(scale);
            sceneInfo.setSceneMap(mapBytes);
            session.saveOrUpdate(sceneInfo);
            isSucceed = true;
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            isSucceed = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        sceneInfoCache.clear();
        return isSucceed;
    }

    /**
     * 得到对应场景的所有WiFi指纹
     *
     * @param scene_name 场景名称
     * @return 返回由WifiFingerprint对象组成的List, 每个WifiFingerprint对应一个位置的WiFi位置指纹
     */
    public List<WifiFingerprint> getWifiFingerprint(String scene_name) {
        if (wifiFingerprintCache.containsKey(scene_name)) {
            return wifiFingerprintCache.get(scene_name);
        }
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<WifiFingerprint> result = session
                .createQuery("from WifiFingerprint where sceneId = ?")
                .setInteger(0, getSceneId(scene_name))
                .list();
        tx.commit();
        session.close();
        wifiFingerprintCache.put(scene_name, result);
        return result;
    }

    /**
     * 得到对应场景的所有地磁指纹
     *
     * @param scene_name 场景名称
     * @return 返回由GeoFingerprint对象组成的List, 每个GeoFingerprint对应一个位置的地磁位置指纹
     */
    public List<GeoFingerprint> getGeoFingerprint(String scene_name) {
        if (geoFingerprintCache.containsKey(scene_name)) {
            return geoFingerprintCache.get(scene_name);
        }
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<GeoFingerprint> result = session
                .createQuery("from GeoFingerprint where sceneId = ?")
                .setInteger(0, getSceneId(scene_name))
                .list();
        tx.commit();
        session.close();
        geoFingerprintCache.put(scene_name, result);
        return result;
    }

    /**
     * 得到数据库中所有场景的名称以及最后更新时间
     *
     * @return 返回一个List存储SceneInfo对象, 包含场景名称, 比例尺, 最后更新时间的信息
     */
    public List<SceneInfo> getSceneList() {
        if (sceneInfoCache.size() != 0) {
            return sceneInfoCache;
        }
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<SceneInfo> result = session.createQuery
                ("select new SceneInfo(sceneName, scale, lastUpdateTime) from SceneInfo ")
                .list();
        tx.commit();
        session.close();
        sceneInfoCache = result;
        return result;
    }

    /**
     * 根据场景名得到场景ID
     *
     * @param scene_name 场景名称
     * @return 返回场景ID
     */
    public int getSceneId(String scene_name) {
        if (sceneIdCache.containsKey(scene_name)) {
            return sceneIdCache.get(scene_name);
        }
        int id;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        SceneInfo sceneInfo = (SceneInfo) session.createQuery("from SceneInfo where sceneName = ?")
                .setString(0, scene_name).uniqueResult();
        if (sceneInfo != null) {
            id = sceneInfo.getId();
            sceneIdCache.put(scene_name, id);
        } else {
            id = -1;
        }
        tx.commit();
        session.close();
        return id;
    }

    /**
     * 根据场景名得到场景地图的字节数组
     *
     * @param scene_name 场景名称
     * @return 返回场景对应地图的字节数组, 若场景地图不存在, 返回null
     */
    public byte[] getSceneMap(String scene_name) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        byte[] result = (byte[]) session
                .createQuery("select sceneMap from SceneInfo where sceneName = ?")
                .setString(0, scene_name)
                .uniqueResult();
        tx.commit();
        session.close();
        return result;
    }
}
