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

import eu.europa.ec.dgc.generation.DccTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DccTestBuilderTest {
    @Test
    void requiredFieldsFormat() {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        dccTestBuilder.fn("Tester");
        dccTestBuilder.fnt("TESTER");
        Assertions.assertThrows(IllegalStateException.class, dccTestBuilder::toJsonString);
    }

    @Test
    void patternMatch() {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            dccTestBuilder.fnt("tester");
        });
    }

    @Test
    void genTest() {
        System.out.println(DgcCryptedPublisherTest.genSampleJson());
    }
}
