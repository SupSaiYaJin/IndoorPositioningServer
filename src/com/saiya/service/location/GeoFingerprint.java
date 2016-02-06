package com.saiya.service.location;

/**
 * 代表一个位置的地磁指纹信息
 */
public class GeoFingerprint {

    /** 位置坐标 */
    private float[] mLoaction;

    /** Y方向的磁场强度 */
    private float mGeomagnetic_y;

    /** Z方向的磁场强度 */
    private float mGeomagnetic_z;

    /**
     * 构造一个位置的地磁指纹信息
     * @param loaction 位置坐标
     * @param geomagnetic_y Y方向的磁场强度
     * @param geomagnetic_z Z方向的磁场强度
     */
    public GeoFingerprint(float[] loaction, float geomagnetic_y, float geomagnetic_z) {
        mLoaction = loaction;
        mGeomagnetic_y = geomagnetic_y;
        mGeomagnetic_z = geomagnetic_z;
    }

    /**
     * 计算当前地磁指纹位置与参数指定的指纹信息的欧氏距离
     * @param geomagnetic_y Y方向的磁场强度
     * @param geomagnetic_z Z方向的磁场强度
     * @return 返回当前地磁指纹位置与参数指定的指纹信息的欧氏距离
     */
    public float getEuclideanDist(float geomagnetic_y, float geomagnetic_z) {
        return (float) Math.sqrt(Math.pow((mGeomagnetic_y - geomagnetic_y), 2) + Math.pow((mGeomagnetic_z - geomagnetic_z), 2));
    }

    /**
     * 得到该地磁指纹的位置坐标
     * @return 返回地磁指纹位置坐标
     */
    public float[] getLocation() {
        return mLoaction;
    }
}
