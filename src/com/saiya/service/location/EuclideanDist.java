package com.saiya.service.location;

/**
 * 封装与指纹库的欧式距离信息,包括指纹的X,Y坐标和测量点与指纹的欧式距离值
 */
public class EuclideanDist {
    private final double location_x;
    private final double location_y;
    private final double euclideanDist;
    public EuclideanDist(double location_x, double location_y, double euclideanDist) {
        this.location_x = location_x;
        this.location_y = location_y;
        this.euclideanDist = euclideanDist;
    }

    public double getLocation_x() {
        return location_x;
    }

    public double getLocation_y() {
        return location_y;
    }

    public double getEuclideanDist() {
        return euclideanDist;
    }
}
