package chapter03.application;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AddRankingTest {
    RankingService rankingService;

    @BeforeClass
    public void setupOnce() {
        rankingService = new HibernateRankingService();

        rankingService.populateRankingData();
    }

    @Test
    public void addRanking() {
        rankingService.addRanking("J. C. Smell", "Drew Lombardo", "Mule", 8);
        assertEquals(rankingService.getRankingFor("J. C. Smell", "Mule"), 8);
    }
}
