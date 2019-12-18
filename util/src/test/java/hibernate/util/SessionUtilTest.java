package hibernate.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class SessionUtilTest {

    @Test
    public void testSessionFactory() {
        try(var session = SessionUtil.getSession()){
            assertNotNull(session);
        }
    }
}
