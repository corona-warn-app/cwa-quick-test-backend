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

package app.coronawarn.quicktest.converter;

import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import java.util.Base64;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class QuickTestArchiveToArchiveCipherDtoV1Converter implements Converter<QuickTestArchive, ArchiveCipherDtoV1> {

    @Override
    public ArchiveCipherDtoV1 convert(final QuickTestArchive quickTest) {
        final ArchiveCipherDtoV1 cipherDto = new ArchiveCipherDtoV1();
        cipherDto.setHashedGuid(quickTest.getHashedGuid());
        cipherDto.setShortHashedGuid(quickTest.getShortHashedGuid());
        cipherDto.setTenantId(quickTest.getTenantId());
        cipherDto.setPocId(quickTest.getPocId());
        cipherDto.setCreatedAt(quickTest.getCreatedAt());
        cipherDto.setUpdatedAt(quickTest.getUpdatedAt());
        cipherDto.setVersion(quickTest.getVersion());
        cipherDto.setConfirmationCwa(quickTest.getConfirmationCwa());
        cipherDto.setTestResult(quickTest.getTestResult());
        cipherDto.setPrivacyAgreement(quickTest.getPrivacyAgreement());
        cipherDto.setLastName(quickTest.getLastName());
        cipherDto.setFirstName(quickTest.getFirstName());
        cipherDto.setEmail(quickTest.getEmail());
        cipherDto.setPhoneNumber(quickTest.getPhoneNumber());
        cipherDto.setSex(quickTest.getSex());
        cipherDto.setStreet(quickTest.getStreet());
        cipherDto.setHouseNumber(quickTest.getHouseNumber());
        cipherDto.setZipCode(quickTest.getZipCode());
        cipherDto.setCity(quickTest.getCity());
        cipherDto.setTestBrandId(quickTest.getTestBrandId());
        cipherDto.setTestBrandName(quickTest.getTestBrandName());
        cipherDto.setBirthday(quickTest.getBirthday());
        cipherDto.setTestResultServerHash(quickTest.getTestResultServerHash());
        cipherDto.setDcc(quickTest.getDcc());
        cipherDto.setAdditionalInfo(quickTest.getAdditionalInfo());
        cipherDto.setGroupName(quickTest.getGroupName());
        return cipherDto;
    }
}
