/*-
 * ---license-start
 * EU-Federation-Gateway-Service / efgs-federation-gateway
 * ---
 * Copyright (C) 2020 - 2021 T-Systems International GmbH and all other contributors
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

import app.coronawarn.quicktest.model.Sex;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.PersistenceException;

@Converter
public class DbEncryptionSexTypeConverter implements AttributeConverter<Sex, String> {

    @Override
    public String convertToDatabaseColumn(Sex s) {
        try {
            return s == null ? null : DbEncryptionService.getInstance().encryptString(s.name());
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Sex convertToEntityAttribute(String s) {
        try {
            return s == null ? null : Sex.valueOf(DbEncryptionService.getInstance().decryptString(s));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

}
