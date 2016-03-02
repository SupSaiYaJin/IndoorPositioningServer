package com.saiya.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Hibernate工具类
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory=null;

    private HibernateUtil(){
    }

    static {
        Configuration cfg = new Configuration().configure();
        ServiceRegistry registry = new ServiceRegistryBuilder()
                .applySettings(cfg.getProperties()).buildServiceRegistry();
        sessionFactory = cfg.buildSessionFactory(registry);
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession(){
        return  sessionFactory.openSession();
    }
}
