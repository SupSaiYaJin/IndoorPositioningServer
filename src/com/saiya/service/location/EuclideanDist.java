package com.saiya.service.location;

/**
 * 封装与指纹库的欧式距离信息,包括指纹的X,Y坐标和测量点与指纹的欧式距离值
 */
public class EuclideanDist {
    private final float location_x;
    private final float location_y;
    private final float euclideanDist;
    public EuclideanDist(float location_x, float location_y, float euclideanDist) {
        this.location_x = location_x;
        this.location_y = location_y;
        this.euclideanDist = euclideanDist;
    }

    public float getLocation_x() {
        return location_x;
    }

    public float getLocation_y() {
        return location_y;
    }

    public float getEuclideanDist() {
        return euclideanDist;
    }
}
