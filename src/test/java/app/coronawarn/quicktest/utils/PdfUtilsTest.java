package app.coronawarn.quicktest.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdfUtilsTest {

    @Test
    void splitLongText() {
        String s250 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
          "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo" +
          " dolores et ea rebum. Stet clita kasd gubergren, no sea tak";

        List<String> strings = PdfUtils.splitStringToParagraph(s250, 50);
        assertThat(strings.size()).isGreaterThan(1);
        assertThat(strings).filteredOn(line -> line.trim().length() > 50).isEmpty();
    }
}