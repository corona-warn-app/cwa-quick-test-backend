package eu.europa.ec.dgc;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base45EncoderTest {
    @Test
    void encodingDecoding() throws Exception {
        for (int i = 16; i<20; i++) {
            byte[] in = new byte[i];
            Random rnd = new Random();
            rnd.nextBytes(in);

            String encoded = Base45Encoder.encodeToString(in);
            byte[] out = Base45Encoder.decodeFromString(encoded);
            assertArrayEquals(in, out);
        }
    }
}