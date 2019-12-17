package chapter03.simple;

import chapter03.hibernate.Person;
import org.testng.annotations.Test;

public class ModelTest {

    @Test
    public void testModelCreation() {
        var subject = new Person();
        subject.setName("John Subjector");

        var observer = new Person();
        observer.setName("Danny Observer");

        var skill = new Skill();
        skill.setName("Java");

        var ranking = new Ranking();
        ranking.setSubject(subject);
        ranking.setObserver(observer);
        ranking.setSkill(skill);
        ranking.setRanking(8);

        System.out.println("Ranking:: " + ranking.toString());
    }
}
