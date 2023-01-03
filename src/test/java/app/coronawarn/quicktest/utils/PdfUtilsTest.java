/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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
