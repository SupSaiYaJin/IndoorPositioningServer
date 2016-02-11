package com.saiya.dao;

import com.saiya.service.location.Algorithms;
import com.saiya.service.location.GeoFingerprint;
import com.saiya.service.location.SceneInfo;
import com.saiya.service.location.WifiFingerprint;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责所有与数据库有关的操作
 */
public class DatabaseManager {

    private DataSource mDataSource;
    private static DatabaseManager mDatabaseManager;

    static {
        wifiFingerprintCache = new HashMap<>();
        geoFingerprintCache = new HashMap<>();
        sceneIdCache = new HashMap<>();
        sceneInfoCache = new ArrayList<>();
        try {
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/positioning");
            mDatabaseManager = new DatabaseManager(dataSource);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param dataSource 数据源对象,所有对数据库的操作通过它完成
     */
    private DatabaseManager(DataSource dataSource) {
        mDataSource = dataSource;
    }

    /**
     * @return 返回DatabaseManager的唯一实例
     */
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
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录信息码
     */
    public int login(String username, String password) {
        try (Connection conn = mDataSource.getConnection()) {
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT password FROM p_user WHERE username = ?");
            Pstmt.setString(1, username);
            ResultSet resultSet = Pstmt.executeQuery();
            return resultSet.next() ? (resultSet.getString("password").equals(password) ?
                    LOGIN_SUCCEED : PASSWORD_ERROR) : USERNAME_NOT_EXIST;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return UNEXPECTED_ERROR;
    }

    /**
     * 注册成功
     */
    public static final int REGISTER_SUCCEED = 3;

    /**
     * 用户名重复
     */
    public static final int DUPLICATE_USERNAME = 4;

    /**
     * 注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册信息码
     */
    public int register(String username, String password) {
        try (Connection conn = mDataSource.getConnection()) {
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT * FROM p_user WHERE username = ?");
            Pstmt.setString(1, username);
            ResultSet resultSet = Pstmt.executeQuery();
            if (resultSet.next()) {
                return DUPLICATE_USERNAME;
            } else {
                Pstmt = conn.prepareStatement("INSERT INTO p_user VALUES (NULL ,?, ?)");
                Pstmt.setString(1, username);
                Pstmt.setString(2, password);
                Pstmt.executeUpdate();
                return REGISTER_SUCCEED;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return UNEXPECTED_ERROR;
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
    public boolean updateWifiFingerPrint
    (String scene_name, float location_x, float location_y, String mac, String rssi) {
        try (Connection conn = mDataSource.getConnection()) {
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                return false;
            }
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT * FROM p_wifi_fingerprint WHERE scene_id = ? AND ABS(location_x - ?) < 1e-5 AND ABS(location_y - ?) < 1e-5",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Pstmt.setInt(1, scene_id);
            Pstmt.setFloat(2, location_x);
            Pstmt.setFloat(3, location_y);
            ResultSet resultSet = Pstmt.executeQuery();
            if (resultSet.next()) {
                resultSet.updateString("mac", mac);
                resultSet.updateString("rssi", rssi);
                resultSet.updateRow();
            } else {
                Pstmt = conn.prepareStatement
                        ("INSERT INTO p_wifi_fingerprint VALUES (NULL,?,?,?,?,?)");
                Pstmt.setInt(1, scene_id);
                Pstmt.setFloat(2, location_x);
                Pstmt.setFloat(3, location_y);
                Pstmt.setString(4, mac);
                Pstmt.setString(5, rssi);
                Pstmt.executeUpdate();
            }
            wifiFingerprintCache.remove(scene_name);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
    public boolean updateGeoFingerprint
    (String scene_name, float location_x,
     float location_y, float geomagnetic_y, float geomagnetic_z) {
        try (Connection conn = mDataSource.getConnection()) {
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                return false;
            }
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT * FROM p_geomagnetic_fingerprint WHERE scene_id = ? AND ABS(location_x - ?) < 1e-5 AND ABS(location_y - ?) < 1e-5",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Pstmt.setInt(1, scene_id);
            Pstmt.setFloat(2, location_x);
            Pstmt.setFloat(3, location_y);
            ResultSet resultSet = Pstmt.executeQuery();
            if (resultSet.next()) {
                resultSet.updateFloat("geomagnetic_y", geomagnetic_y);
                resultSet.updateFloat("geomagnetic_z", geomagnetic_z);
                resultSet.updateRow();
            } else {
                Pstmt = conn.prepareStatement
                        ("INSERT INTO p_geomagnetic_fingerprint VALUES (NULL, ?, ?, ?, ?, ?)");
                Pstmt.setInt(1, scene_id);
                Pstmt.setFloat(2, location_x);
                Pstmt.setFloat(3, location_y);
                Pstmt.setFloat(4, geomagnetic_y);
                Pstmt.setFloat(5, geomagnetic_z);
                Pstmt.executeUpdate();
            }
            geoFingerprintCache.remove(scene_name);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新场景地图
     *
     * @param scene_name 要更新的场景名称
     * @param scale      场景比例尺,含义为多少个像素对应1米
     * @param scene_map  场景地图的输入流
     * @return 返回true为更新成功, false为更新失败
     */
    public boolean updateSceneMap(String scene_name, float scale, InputStream scene_map) {
        try (Connection conn = mDataSource.getConnection()) {
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                PreparedStatement Pstmt = conn.prepareStatement
                        ("INSERT INTO p_scene_info VALUES (NULL , ?, ?, ?, NULL)");
                Pstmt.setString(1, scene_name);
                Pstmt.setBinaryStream(2, scene_map);
                if (scale != 0) {
                    Pstmt.setFloat(3, scale);
                }
                Pstmt.executeUpdate();
            } else {
                PreparedStatement Pstmt = conn.prepareStatement
                        ("UPDATE p_scene_info SET scene_map = ?, scale = ? WHERE id = ?");
                Pstmt.setBinaryStream(1, scene_map);
                if (scale != 0) {
                    Pstmt.setFloat(2, scale);
                }
                Pstmt.setInt(3, scene_id);
                Pstmt.executeUpdate();
            }
            sceneInfoCache.clear();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 得到对应场景的所有WiFi指纹
     *
     * @param scene_name 场景名称
     * @return 返回由WifiFingerprint对象组成的List, 每个WifiFingerprint对应一个位置的WiFi位置指纹
     */
    public List<WifiFingerprint> getWifiFingerprint(String scene_name) {
        if(wifiFingerprintCache.containsKey(scene_name)) {
            return wifiFingerprintCache.get(scene_name);
        }
        List<WifiFingerprint> result = new ArrayList<>();
        try (Connection conn = mDataSource.getConnection()) {
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                return new ArrayList<>();
            }
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT location_x, location_y, mac, rssi FROM p_wifi_fingerprint WHERE scene_id = ?");
            Pstmt.setInt(1, scene_id);
            ResultSet resultSet = Pstmt.executeQuery();
            while (resultSet.next()) {
                float[] location = new float[2];
                String[] fingerprint = new String[2];
                location[0] = resultSet.getFloat("location_x");
                location[1] = resultSet.getFloat("location_y");
                fingerprint[0] = resultSet.getString("mac");
                fingerprint[1] = resultSet.getString("rssi");
                result.add(new WifiFingerprint(location, Algorithms.macToArray(fingerprint[0]),
                        Algorithms.rssiToArray(fingerprint[1])));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if(geoFingerprintCache.containsKey(scene_name)) {
            return geoFingerprintCache.get(scene_name);
        }
        List<GeoFingerprint> result = new ArrayList<>();
        try (Connection conn = mDataSource.getConnection()) {
            int scene_id = getSceneId(scene_name);
            if (scene_id == -1) {
                return new ArrayList<>();
            }
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT location_x, location_y, geomagnetic_y, geomagnetic_z FROM p_geomagnetic_fingerprint WHERE scene_id = ?");
            Pstmt.setInt(1, scene_id);
            ResultSet resultSet = Pstmt.executeQuery();
            while (resultSet.next()) {
                float[] location = new float[2];
                float[] fingerprint = new float[2];
                location[0] = resultSet.getFloat("location_x");
                location[1] = resultSet.getFloat("location_y");
                fingerprint[0] = resultSet.getFloat("geomagnetic_y");
                fingerprint[1] = resultSet.getFloat("geomagnetic_z");
                result.add(new GeoFingerprint(location, fingerprint[0], fingerprint[1]));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        geoFingerprintCache.put(scene_name, result);
        return result;
    }

    /**
     * 得到数据库中所有场景的名称以及最后更新时间
     *
     * @return 返回一个List存储SceneInfo对象, 包含场景名称, 比例尺, 最后更新时间的信息
     */
    public List<SceneInfo> getSceneList() {
        if(sceneInfoCache.size() != 0) {
            return sceneInfoCache;
        }
        List<SceneInfo> result = new ArrayList<>();
        try (Connection conn = mDataSource.getConnection()) {
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT scene_name, scale, last_update_time FROM p_scene_info");
            ResultSet resultSet = Pstmt.executeQuery();
            while (resultSet.next()) {
                result.add(new SceneInfo(resultSet.getString("scene_name"),
                        resultSet.getFloat("scale"), resultSet.getLong("last_update_time")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if(sceneIdCache.containsKey(scene_name)) {
            return sceneIdCache.get(scene_name);
        }
        try (Connection conn = mDataSource.getConnection()) {
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT id FROM p_scene_info WHERE scene_name = ?");
            Pstmt.setString(1, scene_name);
            ResultSet resultSet = Pstmt.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                sceneIdCache.put(scene_name, id);
                return id;
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 根据场景名得到场景地图的字节数组
     *
     * @param scene_name 场景名称
     * @return 返回场景对应地图的字节数组, 若场景地图不存在, 返回null
     */
    public byte[] getSceneMap(String scene_name) {
        try (Connection conn = mDataSource.getConnection()) {
            PreparedStatement Pstmt = conn.prepareStatement
                    ("SELECT scene_map FROM p_scene_info WHERE scene_name = ?");
            Pstmt.setString(1, scene_name);
            ResultSet resultSet = Pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBytes("scene_map");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
