package eu.europa.ec.dgc;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DccTestBuilderTest {
    @Test
    void dateFormat() throws Exception {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        LocalDateTime localNow = LocalDateTime.now();
        System.out.println(dccTestBuilder.toISO8601(localNow));
        assertNotNull(localNow);
    }
}