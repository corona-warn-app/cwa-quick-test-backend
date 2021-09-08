package app.coronawarn.quicktest.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Test
    void formatGermanDate() {
        var localDateTime = LocalDateTime.of(2021, 4, 8, 8, 11, 12);
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        String result = PdfUtils.getFormattedTime(localDateTime, formatter);
        assertThat(result).isEqualTo("08.04.2021 10:11:12");
    }

    @Test
    void formatNullDate() {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        String result = PdfUtils.getFormattedTime(null, formatter);
        assertThat(result).isEqualTo("-");
    }
}