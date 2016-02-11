package com.saiya.service.location;

import java.util.HashMap;
import java.util.Map;

/**
 * 代表一个位置的WiFi指纹信息
 */
public class WifiFingerprint {

    /** 位置坐标 */
    private float[] mLocation;
    /** 收不到信号时的强度 */
    private static final double NONE_RSSI = -90;

    /** 存放指纹信息的Map,键为MAC地址,值为对应MAC地址的RSSI */
    private Map<String, Float> mFingerprintMap;

    /**
     * 构造一个位置的WiFi指纹信息
     * @param location 位置坐标
     * @param mac 解析后的MAC地址
     * @param rssi 解析后的RSSI
     */
    public WifiFingerprint(float[] location, String[] mac, float[] rssi) {
        mLocation = location;
        mFingerprintMap = new HashMap<>();
        for(int i = 0; i < mac.length; ++i) {
            mFingerprintMap.put(mac[i], rssi[i]);
        }
    }

    /**
     * 计算当前WiFi指纹位置与参数指定的指纹信息的欧氏距离
     * @param mac 解析后的MAC地址
     * @param rssi 解析后的RSSI
     * @return 返回当前WiFi指纹位置与参数指定的指纹信息的欧氏距离
     */
    public float getEuclideanDist(String[] mac, float[] rssi) {
        float EuclideanDist = 0;
        for(int i = 0; i < mac.length; ++i) {
            Float rssiFingerprint = mFingerprintMap.get(mac[i]);
            if(rssiFingerprint != null) {
                EuclideanDist += Math.pow(rssiFingerprint - rssi[i], 2);
            }
            else {
                EuclideanDist += Math.pow(NONE_RSSI - rssi[i], 2);
            }
        }
        EuclideanDist = (float) Math.sqrt(EuclideanDist);
        return EuclideanDist;
    }

    /**
     * 得到该WiFi指纹的位置坐标
     * @return 返回位置坐标
     */
    public float[] getLocation() {
        return mLocation;
    }

}
