package com.saiya.tests;

import com.saiya.dao.hbm.GeoFingerprint;
import com.saiya.dao.hbm.SceneInfo;
import com.saiya.dao.hbm.WifiFingerprint;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.Test;

import java.util.Set;

/**
 * Hibernate测试类
 */

public class HibernateTest {

    @Test
    public void testHibernate() {
        Configuration cfg = new Configuration().configure();
        ServiceRegistry registry = new ServiceRegistryBuilder()
                .applySettings(cfg.getProperties()).buildServiceRegistry();
        SessionFactory sessionFactory = cfg.buildSessionFactory(registry);
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        SceneInfo sceneInfo = (SceneInfo) session.load(SceneInfo.class, 3);
        Set<WifiFingerprint> wifiFingerprints = sceneInfo.getWifiFingerprints();
        Set<GeoFingerprint> geoFingerprints = sceneInfo.getGeoFingerprints();
        for (WifiFingerprint wifiFingerprint: wifiFingerprints) {
            System.out.println(wifiFingerprint);
        }
        System.out.println("======================================");
        for (GeoFingerprint geoFingerprint : geoFingerprints) {
            System.out.println(geoFingerprint);
        }
        tx.commit();
        session.close();
    }
}
