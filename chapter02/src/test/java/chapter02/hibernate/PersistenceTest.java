package chapter02.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class PersistenceTest {
    SessionFactory sessionFactory;

    @BeforeSuite
    public void setUp() {
        var registry = new StandardServiceRegistryBuilder().configure().build();

        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    final Message helloMessage = new Message("Hello World!");

    @Test
    public void saveMessage() {
        var testMessage = helloMessage;
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.persist(testMessage);
            transaction.commit();
        }
    }

    @Test(dependsOnMethods = "saveMessage")
    public void readMessage() {
        try (var session = sessionFactory.openSession()) {
            var messageList = session.createQuery("from Message", Message.class).list();

            assertEquals(messageList.size(), 1);

//            assert messageList.get(0).id.equals(helloMessage.id);
//            assert messageList.get(0).text.equals(helloMessage.text);
            // instead of above we can use AssertJ's method
            assertThat(messageList.get(0)).isEqualToComparingFieldByField(helloMessage);

            for (var message : messageList) {
                System.out.println(message);
            }
        }
    }
}
