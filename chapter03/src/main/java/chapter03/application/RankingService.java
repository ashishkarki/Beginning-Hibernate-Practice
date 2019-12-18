package chapter03.application;

import chapter03.hibernate.Person;
import chapter03.hibernate.Skill;
import org.hibernate.Session;

public interface RankingService {
    int getRankingFor(String subject, String skill);

    void addRanking(String subject, String observer, String skill, int ranking);

    Person savePerson(Session session, String nameToSave);

    Skill saveSkill(Session session, String skillToSave);

    void populateRankingData();
}
