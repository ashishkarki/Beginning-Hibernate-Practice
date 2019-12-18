package chapter03.application;

import chapter03.hibernate.Person;
import chapter03.hibernate.Ranking;
import chapter03.hibernate.Skill;
import hibernate.util.SessionUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

public class HibernateRankingService implements RankingService {

    private void addRanking(Session session, String subjectName, String observerName, String skillName, int rank) {
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

    @Override
    public void addRanking(String subject, String observer, String skill, int ranking) {
        try (var session = SessionUtil.getSession()) {
            var transaction = session.beginTransaction();

            addRanking(session, subject, observer, skill, ranking);

            transaction.commit();
        }
    }

    private Person findPerson(Session session, String nameToFind) {
        var query = session.createQuery("from Person p where p.name = :name", Person.class);
        query.setParameter("name", nameToFind);

        return query.uniqueResult();
    }

    @Override
    public Person savePerson(Session session, String nameToSave) {
        var person = findPerson(session, nameToSave);

        if (person == null) {
            person = new Person();
            person.setName(nameToSave);
            session.save(person);
        }

        return person;
    }

    @Override
    public Skill saveSkill(Session session, String skillToSave) {
        var skill = new Skill();
        skill.setName(skillToSave);

        session.save(skill);

        return skill;
    }

    @Override
    public int getRankingFor(String subjectName, String skillName) {
        try (var session = SessionUtil.getSession()) {
            var transaction = session.beginTransaction();

            int average = getRankingFor(session, subjectName, skillName);

            transaction.commit();

            return average;
        }
    }

    private int getRankingFor(Session session, String subjectName, String skillName) {
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

        return (int) statistics.getAverage();
    }

    @Override
    public void populateRankingData() {
        try (var session = SessionUtil.getSession()) {
            var transaction = session.beginTransaction();

            addRanking(session, "J. C. Smell", "Gene Showrama", "Java", 6);
            addRanking(session, "J. C. Smell", "Scottball Most", "Java", 7);
            addRanking(session, "J. C. Smell", "Drew Lombardo", "Java", 8);

            transaction.commit();
        }
    }
}
