/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import javax.persistence.PersistenceException;

public class DbEncryptionLocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

  @Override
  public String convertToDatabaseColumn(LocalDateTime s) {
    try {
      return s==null ? null : DbEncryptionService.getInstance().encryptLocalDateTime(s);
    } catch (InvalidAlgorithmParameterException | InvalidKeyException 
            | BadPaddingException | IllegalBlockSizeException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public LocalDateTime convertToEntityAttribute(String s) {
    try {
      return s==null ? null : DbEncryptionService.getInstance().decryptLocalDateTime(s);
    } catch (InvalidAlgorithmParameterException | InvalidKeyException 
            | BadPaddingException | IllegalBlockSizeException e) {
      throw new PersistenceException(e);
    }
  }

}
