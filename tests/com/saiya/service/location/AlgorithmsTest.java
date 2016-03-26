package com.saiya.service.location;

import org.junit.Assert;
import org.junit.Test;

/**
 * Algorithms测试
 */
public class AlgorithmsTest {

    @Test
    public void testLocateScene() {
        Assert.assertEquals("家", Algorithms.locateScene("b8:55:10:13:22:2d,f0:eb:d0:56:af:65,08:10:76:27:18:02,94:77:2b:0c:85:a8,0c:72:2c:4e:12:5e,00:0c:50:01:17:58,78:a1:06:a4:d6:c8,c8:e7:d8:34:bb:56,cc:a2:23:7d:4a:ec,bc:14:ef:a2:0a:3e"));
    }

}