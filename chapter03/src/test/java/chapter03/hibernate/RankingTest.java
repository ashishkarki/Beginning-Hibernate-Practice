package chapter03.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RankingTest {

    SessionFactory sessionFactory;

    @BeforeMethod
    public void setup() {
        var registry = new StandardServiceRegistryBuilder().configure().build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    @AfterMethod
    public void teardown() {
        sessionFactory.close();
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

    @Test
    public void testChangeRanking() {
        populateRankingData();

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var ranking = findRanking(session, "J. C. Smell", "Gene Showrama", "Java");

            assertNotNull(ranking, "Could not find matching ranking");
            ranking.setRanking(9); // this is the updating ranking value

            transaction.commit();
        }

        assertThat(getAverage("J. C. Smell", "Java")).isEqualTo(8);
    }

    @Test
    public void testRemoveRanking() {
        populateRankingData();

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var ranking = findRanking(session, "J. C. Smell", "Gene Showrama", "Java");
            assertNotNull(ranking, "No ranking found for this combination");

            session.delete(ranking);

            transaction.commit();
        }

        // verify that ranking was deleted and that there is new average ranking
        assertThat(getAverage("J. C. Smell", "Java")).isEqualTo(7);
    }

    private Ranking findRanking(Session session, String subjectName, String observerName, String skillName) {
        Query<Ranking> query = session.createQuery(
                "from Ranking r "
                        + "where r.subject.name = :subject and "
                        + "r.observer.name = :observer and "
                        + "r.skill.name = :skill"
                , Ranking.class);

        query.setParameter("subject", subjectName);
        query.setParameter("observer", observerName);
        query.setParameter("skill", skillName);

        // get the one unique result with this combination
        var ranking = query.uniqueResult();

        return ranking;
    }

    private int getAverage(String subjectName, String skillName) {
        try (var session = sessionFactory.openSession()) {
            var transcation = session.beginTransaction();

            Query<Ranking> rankingQuery = session.createQuery(
                    "from Ranking r "
                            + "where r.subject.name = :subject and "
                            + "r.skill.name = :skill"
                    , Ranking.class);

            rankingQuery.setParameter("subject", subjectName);
            rankingQuery.setParameter("skill", skillName);

            IntSummaryStatistics statistics = rankingQuery.list()
                    .stream()
                    .collect(Collectors.summarizingInt(Ranking::getRanking));

            int average = (int) statistics.getAverage();

            transcation.commit();

            return average;
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