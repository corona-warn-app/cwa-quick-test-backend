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

import java.io.ByteArrayOutputStream;

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

    /**
     * decode base45 string to bytes.
     * @param encodedString string
     * @return bytes
     */
    public static byte[] decodeFromString(String encodedString) {
        int remainderSize = encodedString.length() % 3;
        if (remainderSize == 1) {
            new IllegalArgumentException("wrong remainder length: " + remainderSize);
        }
        int wholeChunkCount = encodedString.length() / 3;
        byte[] result = new byte[wholeChunkCount * 2 + (remainderSize == 2 ? 1 : 0)];
        int resultIndex = 0;
        int wholeChunkLength = wholeChunkCount * 3;
        for (int i = 0;  i < wholeChunkLength; ) {
            int c0 = ALPHABET.indexOf(encodedString.charAt(i++));
            int c1 = ALPHABET.indexOf(encodedString.charAt(i++));
            int c2 = ALPHABET.indexOf(encodedString.charAt(i++));
            if (c0 < 0 || c1 < 0 || c2 < 0) {
                new IllegalArgumentException("unsupported input character near pos: " + i);
            }
            int val = c0 + 45 * c1 + 45 * 45 * c2;
            if (val > 0xFFFF) {
                throw new IllegalArgumentException();
            }
            result[resultIndex++] = (byte)(val / 256);
            result[resultIndex++] = (byte)(val % 256);
        }

        if (remainderSize != 0) {
            int c0 = ALPHABET.indexOf(encodedString.charAt(encodedString.length() - 2));
            int c1 = ALPHABET.indexOf(encodedString.charAt(encodedString.length() - 1));
            result[resultIndex] = (byte) (c0 + 45 * c1);
        }
        return result;
    }
}
