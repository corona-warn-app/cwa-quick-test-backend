/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for PDF generation.
 */
public final class PdfUtils {

    /**
     * Split a String by whitespace and return a list of lines with a certain limit.
     * @param value The String to be split.
     * @param limit Line limit.
     * @return List of Strings to be printed as a paragraph.
     */
    public static List<String> splitStringToParagraph(String value, int limit) {
        List<String> textParagraph = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder();
        String[] split = value.split(" ");
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            String word = split[i];
            lineBuilder.append(word).append(" ");
            if (i == splitLength - 1 || lineBuilder.length() + split[i + 1].length() > limit) {
                textParagraph.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
            }
        }
        return textParagraph;
    }

    /**
     * Return a formatted LocalDateTime with Timezone Berlin.
     * @param ldt The LocaldateDatime
     * @param formatter The formatter.
     * @return Formatted String or hyphen
     */
    public static String getFormattedTime(LocalDateTime ldt, DateTimeFormatter formatter) {
        String dateAndTimeInGermany;
        if (ldt != null) {
            dateAndTimeInGermany =
              ZonedDateTime.of(ldt, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(formatter);
        } else {
            dateAndTimeInGermany = "-";
        }
        return dateAndTimeInGermany;
    }

}
