package chapter03.hibernate;

import chapter03.application.HibernateRankingService;
import chapter03.application.RankingService;
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

    RankingService rankingService;

    @BeforeMethod
    public void setup() {
        var registry = new StandardServiceRegistryBuilder().configure().build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();

        rankingService = new HibernateRankingService();
    }

    @AfterMethod
    public void teardown() {
        sessionFactory.close();
    }

    @Test
    public void testSaveRanking() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            Person subject = rankingService.savePerson(session, "J. C. Smell");
            Skill skill = rankingService.saveSkill(session, "Java");

            Person observer = rankingService.savePerson(session, "Drew Lombardo");

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
        rankingService.populateRankingData();

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
        rankingService.populateRankingData();

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var ranking = findRanking(session, "J. C. Smell", "Gene Showrama", "Java");

            assertNotNull(ranking, "Could not find matching ranking");
            ranking.setRanking(9); // this is the updating ranking value

            transaction.commit();
        }

        assertThat(rankingService.getRankingFor("J. C. Smell", "Java")).isEqualTo(8);
    }

    @Test
    public void testRemoveRanking() {
        rankingService.populateRankingData();

        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var ranking = findRanking(session, "J. C. Smell", "Gene Showrama", "Java");
            assertNotNull(ranking, "No ranking found for this combination");

            session.delete(ranking);

            transaction.commit();
        }

        // verify that ranking was deleted and that there is new average ranking
        assertThat(rankingService.getRankingFor("J. C. Smell", "Java")).isEqualTo(7);
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
        return query.uniqueResult();
    }
}