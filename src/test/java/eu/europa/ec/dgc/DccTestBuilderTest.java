package eu.europa.ec.dgc;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DccTestBuilderTest {
    @Test
    void requiredFieldsFormat() throws Exception {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        dccTestBuilder.fn("Tester");
        dccTestBuilder.fnt("TESTER");
        Assertions.assertThrows(IllegalStateException.class, () -> {
            dccTestBuilder.toJsonString();
        });
    }

    @Test
    void patternMatch() throws Exception {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            dccTestBuilder.fnt("tester");
        });
    }

    @Test
    void genTest() throws Exception {
        System.out.println(DgcCryptedPublisherTest.genSampleJson());
    }

}