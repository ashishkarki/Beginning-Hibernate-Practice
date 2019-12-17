package chapter03.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class RankingTest {

    SessionFactory sessionFactory;

    @BeforeClass
    public void setup() {
        var registry = new StandardServiceRegistryBuilder().configure().build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    @Test
    public void testSaveRanking() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            Person subject = savePerson(session, "J. C. Smell");
            Skill skill = saveSkill(session, "Java");

            Person observer = savePerson(session, "Drew Lombardo");

            Ranking ranking = new Ranking();
            ranking.setSubject(subject);
            ranking.setObserver(observer);
            ranking.setSkill(skill);
            ranking.setRanking(8);
            session.save(ranking);

            transaction.commit();
        }
    }

    @Test
    public void testRankings() {
        populateRankingData();

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            Query<Ranking> query = session.createQuery(
                    "from Ranking r "
                            + "where r.subject.name = :name "
                            + "and r.skill.name = :skill"
                    , Ranking.class);
            query.setParameter("name", "J. C. Smell");
            query.setParameter("skill", "Java");

            IntSummaryStatistics statistics = query.list()
                    .stream()
                    .collect(Collectors.summarizingInt(Ranking::getRanking));

            long count = statistics.getCount();
            int average = (int) statistics.getAverage();

            transaction.commit();

            session.close();

            assertEquals(count, 3);
            assertEquals(average, 7);
        }
    }

    private Person findPerson(Session session, String nameToFind) {
        var query = session.createQuery("from Person p where p.name = :name", Person.class);
        query.setParameter("name", nameToFind);
        var person = query.uniqueResult();

        return person;
    }

    private Person savePerson(Session session, String nameToSave) {
        var person = findPerson(session, nameToSave);

        if (person == null) {
            person = new Person();
            person.setName(nameToSave);
            session.save(person);
        }

        return person;
    }

    private Skill saveSkill(Session session, String skillToSave) {
        var skill = new Skill();
        skill.setName(skillToSave);

        session.save(skill);

        return skill;
    }

    private void populateRankingData() {
        try (var session = sessionFactory.openSession()) {
            var transcation = session.beginTransaction();

            createData(session, "J. C. Smell", "Gene Showrama", "Java", 6);
            createData(session, "J. C. Smell", "Scottball Most", "Java", 7);
            createData(session, "J. C. Smell", "Drew Lombardo", "Java", 8);
            transcation.commit();
        }
    }

    // create a new complex Person
    private void createData(
            Session session,
            String subjectName,
            String observerName,
            String skillName,
            int rank) {
        var subject = savePerson(session, subjectName);
        var observer = savePerson(session, observerName);
        var skill = saveSkill(session, skillName);

        Ranking ranking = new Ranking();
        ranking.setSubject(subject);
        ranking.setObserver(observer);
        ranking.setSkill(skill);
        ranking.setRanking(rank);
        session.save(ranking);
    }
}