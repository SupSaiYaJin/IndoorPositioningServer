package com.saiya.dao;

import org.junit.Assert;
import org.junit.Test;

/**
 * DataBaseManager测试
 */
public class DatabaseManagerTest {

    @Test
    public void testGetSceneId() {
        DatabaseManager.setIsRelease(false);
        int id = DatabaseManager.getInstance().getSceneId("家");
        Assert.assertEquals("获取场景ID", 3, id);
    }

}