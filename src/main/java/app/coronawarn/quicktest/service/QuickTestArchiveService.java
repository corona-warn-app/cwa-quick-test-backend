package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuickTestArchiveService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestRepository quickTestRepository;
    private final QuickTestArchiveRepository quickTestArchiveRepository;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param shortHashedGuid to identify quicktest
     * @param pdf as MultipartFile to store
     * @throws QuickTestServiceException if the pdf was no readable or not storable.
     */
    public void createNewQuickTestArchive(Map<String, String> ids, String shortHashedGuid, MultipartFile pdf) throws QuickTestServiceException {
        QuickTest quickTest = quickTestRepository.findByPocIdAndShortHashedGuid(
            ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHashedGuid);
        if (quickTest == null) {
            log.info("Requested Quick Test with shortHash {} could not be found.", shortHashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.UPDATE_NOT_FOUND);
        }
        try {
            quickTestArchiveRepository.saveAndFlush(mappingQuickTestToQuickTestAchive(quickTest, pdf));
            shortHashedGuid = "123";
            quickTestRepository.deleteById(shortHashedGuid);
        } catch (IOException e) {
            log.error("Could not read pdf. IO Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INTERNAL_ERROR);
        } catch (Exception e) {
            log.error("Exception = {}", e.getMessage());
            QuickTestServiceException ex = new QuickTestServiceException(QuickTestServiceException.Reason.INTERNAL_ERROR);
            throw ex;
        }
    }

    private QuickTestArchive mappingQuickTestToQuickTestAchive(
            QuickTest quickTest, MultipartFile pdf) throws IOException {
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setShortHashedGuid(quickTest.getShortHashedGuid());
        quickTestArchive.setHashedGuid(quickTest.getHashedGuid());
        quickTestArchive.setConfirmationCwa(quickTest.getConfirmationCwa());
        quickTestArchive.setCreatedAt(quickTest.getCreatedAt());
        quickTestArchive.setUpdatedAt(quickTest.getUpdatedAt());
        quickTestArchive.setTenantId(quickTest.getTenantId());
        quickTestArchive.setPocId(quickTest.getPocId());
        quickTestArchive.setTestResult(quickTest.getTestResult());
        quickTestArchive.setInsuranceBillStatus(quickTest.getInsuranceBillStatus());
        quickTestArchive.setFirstName(quickTest.getFirstName());
        quickTestArchive.setLastName(quickTest.getLastName());
        quickTestArchive.setEmail(quickTest.getEmail());
        quickTestArchive.setPhoneNumber(quickTest.getPhoneNumber());
        quickTestArchive.setSex(quickTest.getSex());
        quickTestArchive.setStreet(quickTest.getStreet());
        quickTestArchive.setHouseNumber(quickTest.getHouseNumber());
        quickTestArchive.setZipCode(quickTest.getZipCode());
        quickTestArchive.setCity(quickTest.getCity());
        quickTestArchive.setTestBrandId(quickTest.getTestBrandId());
        quickTestArchive.setTestBrandName(quickTest.getTestBrandName());
        quickTestArchive.setPdf(pdf.getBytes());
        return quickTestArchive;
    }
}
