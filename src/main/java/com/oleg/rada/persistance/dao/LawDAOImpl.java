package com.oleg.rada.persistance.dao;

import com.oleg.rada.persistance.Law;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LawDAOImpl implements LawDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Law law) {
        Session s = sessionFactory.openSession();
        s.saveOrUpdate(law);
        s.close();
    }

    @Override
    public void saveAll(Iterable<Law> laws) {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();
        for (Law law : laws){
            try{
                s.saveOrUpdate(law);
            }catch (Exception ex){
                System.err.println(law.getId() + "\t" + ex.getMessage());
            }
        }
        tx.commit();
        s.close();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
