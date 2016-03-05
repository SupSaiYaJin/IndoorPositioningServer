package com.saiya.dao.hbm;

import javax.persistence.*;

/**
 * 地磁指纹实体类
 */
@Entity
@Table(name = "p_geomagnetic_fingerprint")
public class GeoFingerprint {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "scene_id")
    private int sceneId;

    @Column(name = "location_x")
    private double locationX;

    @Column(name = "location_y")
    private double locationY;

    @Column(name = "geomagnetic_y")
    private double geomagneticY;

    @Column(name = "geomagnetic_z")
    private double geomagneticZ;

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

    public double getGeomagneticY() {
        return geomagneticY;
    }

    public void setGeomagneticY(double geomagneticY) {
        this.geomagneticY = geomagneticY;
    }

    public double getGeomagneticZ() {
        return geomagneticZ;
    }

    public void setGeomagneticZ(double geomagneticZ) {
        this.geomagneticZ = geomagneticZ;
    }

    /**
     * 计算当前地磁指纹位置与参数指定的指纹信息的欧氏距离
     * @param geomagnetic_y Y方向的磁场强度
     * @param geomagnetic_z Z方向的磁场强度
     * @return 返回当前地磁指纹位置与参数指定的指纹信息的欧氏距离
     */
    public double getEuclideanDist(double geomagnetic_y, double geomagnetic_z) {
        return (double) Math.sqrt(Math.pow((geomagneticY - geomagnetic_y), 2)
                + Math.pow((geomagnetic_z - geomagnetic_z), 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoFingerprint that = (GeoFingerprint) o;

        if (id != that.id) return false;
        if (sceneId != that.sceneId) return false;
        if (Double.compare(that.locationX, locationX) != 0) return false;
        if (Double.compare(that.locationY, locationY) != 0) return false;
        if (Double.compare(that.geomagneticY, geomagneticY) != 0) return false;
        if (Double.compare(that.geomagneticZ, geomagneticZ) != 0) return false;

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
        temp = Double.doubleToLongBits(geomagneticY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(geomagneticZ);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GeoFingerprint{" +
                "id=" + id +
                ", sceneId=" + sceneId +
                ", locationX=" + locationX +
                ", locationY=" + locationY +
                ", geomagneticY=" + geomagneticY +
                ", geomagneticZ=" + geomagneticZ +
                '}';
    }

}
