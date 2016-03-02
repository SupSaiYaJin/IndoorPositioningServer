package com.saiya.service.location;

import com.saiya.dao.DatabaseManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 负责所有定位算法
 */
public class Algorithms {
    private Algorithms(){}

    /** 保留的小数位数 */
    private static final int RETAIN_DECIMAL = 4;

    /**
     * 根据参数中的WiFi指纹信息定位
     * @param sceneName 场景名称
     * @param mac 采集到的MAC信息,形式为mac1,mac2...,macN
     * @param rssi 采集到的RSSI信息,形式为rssi1,rssi2...,rssiN
     * @return 返回定位结果坐标
     */
    public static float[] locateOnWifi(String sceneName, String mac, String rssi) {
        List<WifiFingerprint> wifiDataList = DatabaseManager.getInstance()
                .getWifiFingerprint(sceneName);
        List<EuclideanDist> EuclideanDistList = new ArrayList<>();
        for (WifiFingerprint wifiFingerprint : wifiDataList) {
            float location_x = wifiFingerprint.getLocation()[0];
            float location_y = wifiFingerprint.getLocation()[1];
            float euclideanDist = wifiFingerprint.getEuclideanDist(macToArray(mac),
                    rssiToArray(rssi));
            EuclideanDistList.add(new EuclideanDist(location_x, location_y, euclideanDist));
        }
        return calculateByWeight(EuclideanDistList);
    }

    /**
     * 根据参数中的地磁指纹信息定位
     * @param sceneName 场景名称
     * @param geomagnetic_y 采集到的Y方向磁场强度
     * @param geomagnetic_z 采集到的Z方向磁场强度
     * @return 返回定位结果坐标
     */
    public static float[] locateOnGeomagnetic (String sceneName,
            float geomagnetic_y, float geomagnetic_z) {
        List<GeoFingerprint> geoDataList = DatabaseManager.getInstance()
                .getGeoFingerprint(sceneName);
        List<EuclideanDist> EuclideanDistList = new ArrayList<>();
        for (GeoFingerprint geoFingerprint : geoDataList) {
            float location_x = geoFingerprint.getLocation()[0];
            float location_y = geoFingerprint.getLocation()[1];
            float euclideanDist = geoFingerprint.getEuclideanDist(geomagnetic_y, geomagnetic_z);
            EuclideanDistList.add(new EuclideanDist(location_x, location_y, euclideanDist));
        }
        return calculateByWeight(EuclideanDistList);
    }

    /**
     * 根据参数中的WiFi以及地磁指纹信息定位
     * @param sceneName 场景名称
     * @param mac 采集到的MAC信息,形式为mac1,mac2...,macN
     * @param rssi 采集到的RSSI信息,形式为rssi1,rssi2...,rssiN
     * @param geomagnetic_y 采集到的Y方向磁场强度
     * @param geomagnetic_z 采集到的Z方向磁场强度
     * @return 返回定位结果坐标
     */
    public static float[] locateOnAll (String sceneName, String mac, String rssi,
            float geomagnetic_y, float geomagnetic_z) {
        float[] wifiResult = locateOnWifi(sceneName, mac, rssi);
        float[] geoResult = locateOnGeomagnetic(sceneName, geomagnetic_y, geomagnetic_z);
        float[] result = new float[2];
        result[0] = (wifiResult[0] + geoResult[0]) / 2;
        result[1] = (wifiResult[1] + geoResult[1]) / 2;
        return result;
    }

    private static final int K = 3;
    private static final float[] wrongResult = new float[]{-1, -1};

    /**
     * 按权值将K个位置的X,Y坐标分配给结果
     * @param EuclideanDistList 存储EuclideanDist对象的List,
     *                          包含信息有对应指纹数据库的位置坐标以及采集到的指纹与数据库指纹的欧氏距离
     * @return 返回定位结果坐标
     */
    private static float[] calculateByWeight(List<EuclideanDist> EuclideanDistList) {
        if (EuclideanDistList.size() < K) {
            return wrongResult;
        }
        float[] result = new float[2];
        //将List中前K小的欧氏距离放在List的最前面
        findKNearestEuclideanDist(EuclideanDistList, 0, EuclideanDistList.size() - 1, K);
        //存储这K个欧氏距离
        float euclideanDists[] = new float[K];
        //存储这K个欧氏距离对应的位置
        float locations[][] = new float[K][2];
        //K个欧式距离之和,方便计算权重
        float euclideanDistsSum = 0f;
        for (int i = 0; i < K; ++i) {
            locations[i][0] = EuclideanDistList.get(i).getLocation_x();
            locations[i][1] = EuclideanDistList.get(i).getLocation_y();
            euclideanDists[i] = EuclideanDistList.get(i).getEuclideanDist();
            euclideanDistsSum += euclideanDists[i];
        }
        //K个坐标的权重,欧氏距离越小的权重越大
        float[] weight = new float[K];
        for (int i = 0; i < K; ++i) {
            weight[i] = (euclideanDistsSum - euclideanDists[i]) / (2 * (euclideanDistsSum));
        }
        //将权重分配给结果
        for (int i = 0; i < K; ++i) {
            result[0] += weight[i] * locations[i][0];
            result[1] += weight[i] * locations[i][1];
        }
        setDecimalScale(result);
        return result;
    }

    /**
     * 将EuclideanDistList中欧氏距离值最小的前K个值放到最前面
     * @param EuclideanDistList 存储EuclideanDist对象的List,
     *                          包含信息有对应指纹数据库的位置坐标以及采集到的指纹与数据库指纹的欧氏距离
     * @param start 起始位置
     * @param end 终止位置
     * @param K 常量K
     */
    private static void findKNearestEuclideanDist(List<EuclideanDist> EuclideanDistList,
            int start, int end, int K) {
        if (end - start + 1 < K) {
            return;
        }
        if (start < end) {
            int mid = partition(EuclideanDistList, start, end);
            if (mid - start + 1 == K) {
                return;
            }
            if (mid - start + 1 > K) {
                findKNearestEuclideanDist(EuclideanDistList, start, mid - 1, K);
            } else {
                findKNearestEuclideanDist(EuclideanDistList, mid + 1, end, K - (mid - start + 1));
            }
        }
    }

    /**
     * findKNearestEuclideanDist函数的分区函数
     * @param EuclideanDistList 存储EuclideanDist对象的List,
     *                          包含信息有对应指纹数据库的位置坐标以及采集到的指纹与数据库指纹的欧氏距离
     * @param start 起始位置
     * @param end 终止位置
     * @return 分界位置
     */
    private static int partition(List<EuclideanDist> EuclideanDistList, int start, int end) {
        float key = EuclideanDistList.get(end).getEuclideanDist();
        int result = start;
        for (int i = start; i < end; ++i) {
            if (EuclideanDistList.get(i).getEuclideanDist() < key) {
                Collections.swap(EuclideanDistList, result, i);
                ++result;
            }
        }
        Collections.swap(EuclideanDistList, result, end);
        return result;
    }

    /**
     * 设置定位结果的保留小数位数
     * @param nums 定位结果坐标
     */
    private static void setDecimalScale(float[] nums) {
        for (int i = 0; i < nums.length; ++i)
            nums[i] = new BigDecimal(nums[i])
                    .setScale(RETAIN_DECIMAL, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 将采集到的MAC信息解析为String数组
     * @param mac 采集到的MAC信息
     * @return 返回解析后MAC信息数组
     */
    public static String[] macToArray(String mac) {
        return mac.split(",");
    }

    /**
     * 将采集到的RSSI信息解析为float数组
     * @param rssi 采集到的RSSI信息
     * @return 返回解析后的RSSI信息数组
     */
    public static float[] rssiToArray(String rssi) {
        String[] rssiStringArray = rssi.split(",");
        float[] rssiFloatArray = new float[rssiStringArray.length];
        for (int i = 0; i < rssiStringArray.length; ++i) {
            rssiFloatArray[i] = Float.parseFloat(rssiStringArray[i]);
        }
        return rssiFloatArray;
    }
}
