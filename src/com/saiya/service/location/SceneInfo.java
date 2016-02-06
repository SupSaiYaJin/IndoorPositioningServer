package com.saiya.service.location;

/**
 * 封装场景信息的类,包括场景名,比例尺,最后更新时间
 */
public class SceneInfo {
    private final String sceneName;
    private final float scale;
    private final long lastUpdateTime;
    public SceneInfo(String sceneName, float scale, long lastUpdateTime) {
        this.sceneName = sceneName;
        this.scale = scale;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getSceneName() {
        return sceneName;
    }

    public float getScale() {
        return scale;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
