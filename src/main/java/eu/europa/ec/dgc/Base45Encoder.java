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

package eu.europa.ec.dgc;

/**
 * encoder for base45.
 */
public class Base45Encoder {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";

    /**
     * encode to string.
     * @param bytes bytes
     * @return encoded string
     */
    public static String encodeToString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (int i = 0;i < bytes.length;i += 2) {
            if (bytes.length - i > 1) {
                int x = ((bytes[i] & 0xFF) << 8) + (bytes[i + 1] & 0xFF);
                int e = x / (45 * 45);
                int y = x % (45 * 45);
                int d = y / 45;
                int c = y % 45;
                result.append(ALPHABET.charAt(c)).append(ALPHABET.charAt(d)).append(ALPHABET.charAt(e));
            } else {
                int x = bytes[i] & 0xFF;
                int d = x / 45;
                int c = x % 45;
                result.append(ALPHABET.charAt(c)).append(ALPHABET.charAt(d));
            }
        }
        return result.toString();
    }
}
