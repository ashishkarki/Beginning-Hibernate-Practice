package hibernate.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jboss.logging.Logger;

public class SessionUtil {

    private static final SessionUtil instance = new SessionUtil();
    private final SessionFactory sessionFactory;
    private static final String CONFIG_NAME = "/configuration.properties";
    Logger logger = Logger.getLogger(this.getClass());

    private SessionUtil() {
        var registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        sessionFactory = new MetadataSources(registry)
                .buildMetadata()
                .buildSessionFactory();
    }

    private static SessionUtil getInstance() {
        return instance;
    }

    public static Session getSession() {
        return getInstance().sessionFactory.openSession();
    }
}
