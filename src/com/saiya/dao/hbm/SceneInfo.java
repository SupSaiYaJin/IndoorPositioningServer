package com.saiya.dao.hbm;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

/**
 * 场景信息实体类
 */
@Entity
@Table(name = "p_scene_info")
public class SceneInfo {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "scene_name")
    private String sceneName;

    @Lob
    @Column(name = "scene_map")
    private byte[] sceneMap;

    @Column(name = "scale")
    private double scale;

    @Column(name = "last_update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTime;

    public SceneInfo() {}

    public SceneInfo(int id, String sceneName, double scale, Date lastUpdateTime) {
        this.id = id;
        this.sceneName = sceneName;
        this.scale = scale;
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public byte[] getSceneMap() {
        return sceneMap;
    }

    public void setSceneMap(byte[] sceneMap) {
        this.sceneMap = sceneMap;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SceneInfo sceneInfo = (SceneInfo) o;

        if (id != sceneInfo.id) return false;
        if (Double.compare(sceneInfo.scale, scale) != 0) return false;
        if (sceneName != null ? !sceneName.equals(sceneInfo.sceneName) : sceneInfo.sceneName != null)
            return false;
        if (!Arrays.equals(sceneMap, sceneInfo.sceneMap)) return false;
        if (lastUpdateTime != null ? !lastUpdateTime.equals(sceneInfo.lastUpdateTime) : sceneInfo.lastUpdateTime != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (sceneName != null ? sceneName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(sceneMap);
        temp = Double.doubleToLongBits(scale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
        return result;
    }
}
