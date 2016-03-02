package com.saiya.dao.hbm;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * WiFi指纹实体类
 */
@Entity
@Table(name = "p_wifi_fingerprint")
public class WifiFingerprint {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "scene_id")
    private int sceneId;

    @Column(name = "location_x")
    private double locationX;

    @Column(name = "location_y")
    private double locationY;

    @Column(name = "mac")
    private String mac;

    @Column(name = "rssi")
    private String rssi;

    /** 收不到信号时的强度 */
    private static final double NONE_RSSI = -90;

    @Transient
    /** 存放指纹信息的Map,键为MAC地址,值为对应MAC地址的RSSI */
    private Map<String, Double> mFingerprintMap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    /**
     * 计算当前WiFi指纹位置与参数指定的指纹信息的欧氏距离
     * @param mac 解析后的MAC地址
     * @param rssi 解析后的RSSI
     * @return 返回当前WiFi指纹位置与参数指定的指纹信息的欧氏距离
     */
    public double getEuclideanDist(String[] mac, double[] rssi) {
        if (mFingerprintMap == null) {
            buildMap();
        }
        double EuclideanDist = 0;
        for (int i = 0; i < mac.length; ++i) {
            Double rssiFingerprint = mFingerprintMap.get(mac[i]);
            if (rssiFingerprint != null) {
                EuclideanDist += Math.pow(rssiFingerprint - rssi[i], 2);
            } else {
                EuclideanDist += Math.pow(NONE_RSSI - rssi[i], 2);
            }
        }
        EuclideanDist = (float) Math.sqrt(EuclideanDist);
        return EuclideanDist;
    }

    /**
     * 建Map,方便欧氏距离的计算
     */
    private void buildMap() {
        String[] macs = mac.split(",");
        String[] rssiString = rssi.split(",");
        Double[] rssis = new Double[macs.length];
        for (int i = 0; i < rssis.length; ++i) {
            rssis[i] = Double.parseDouble(rssiString[i]);
        }
        mFingerprintMap = new HashMap<>();
        for (int i = 0; i < macs.length; ++i) {
            mFingerprintMap.put(macs[i], rssis[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiFingerprint that = (WifiFingerprint) o;

        if (id != that.id) return false;
        if (sceneId != that.sceneId) return false;
        if (Double.compare(that.locationX, locationX) != 0) return false;
        if (Double.compare(that.locationY, locationY) != 0) return false;
        if (mac != null ? !mac.equals(that.mac) : that.mac != null) return false;
        if (rssi != null ? !rssi.equals(that.rssi) : that.rssi != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + sceneId;
        temp = Double.doubleToLongBits(locationX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(locationY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        result = 31 * result + (rssi != null ? rssi.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WifiFingerprint{" +
                "id=" + id +
                ", sceneId=" + sceneId +
                ", locationX=" + locationX +
                ", locationY=" + locationY +
                ", mac='" + mac + '\'' +
                ", rssi='" + rssi + '\'' +
                '}';
    }
}
