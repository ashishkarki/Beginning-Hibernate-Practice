package chapter03.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PersonTest {

    SessionFactory sessionFactory;

    @BeforeClass
    public void setup() {
        var registry = new StandardServiceRegistryBuilder().configure().build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    @Test
    public void testSavePerson() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var person = new Person();
            person.setName("John Doe");

            session.save(person);

            transaction.commit();
        }
    }
}
