package com.github.blackz.db;

import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.StatelessSession;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;

public class AppHibernate {

    public static void inTransaction(Consumer<StatelessSession> consumer) {
        AppHibernateSessionFactory.getSessionFactory().inStatelessTransaction(consumer);
    }

    public static <R> R fromTransaction(Function<StatelessSession, R> function) {
        return AppHibernateSessionFactory.getSessionFactory().fromStatelessTransaction(function);
    }

    public static void doWork(Work work) {
        AppHibernateSessionFactory.getSessionFactory()
            .inStatelessSession(session -> session.doWork(work));
    }

    public static <T> void doReturningWork(ReturningWork<T> returningWork) {
        AppHibernateSessionFactory.getSessionFactory()
            .inStatelessSession(session -> session.doReturningWork(returningWork));
    }
}
