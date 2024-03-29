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

package app.coronawarn.quicktest.dbencryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.PersistenceException;

@Converter
public class DbEncryptionShortConverter implements AttributeConverter<Short, String> {

    @Override
    public String convertToDatabaseColumn(Short s) {
        try {
            return s == null ? null : DbEncryptionService.getInstance().encryptShort(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Short convertToEntityAttribute(String s) {
        try {
            return s == null ? null : DbEncryptionService.getInstance().decryptShort(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

}
