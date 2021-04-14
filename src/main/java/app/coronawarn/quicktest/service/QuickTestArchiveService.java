package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestArchiveService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestRepository quickTestRepository;
    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final QuickTestStatisticsRepository quickTestStatisticsRepository;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param shortHashedGuid to identify quicktest
     * @param pdf             as MultipartFile to store
     * @throws QuickTestServiceException if the pdf was no readable or not storable.
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNewQuickTestArchive(Map<String, String> ids, String shortHashedGuid, MultipartFile pdf)
        throws QuickTestServiceException {
        QuickTest quickTest = quickTestRepository.findByPocIdAndShortHashedGuid(
            ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHashedGuid);
        if (quickTest == null) {
            log.info("Requested Quick Test with shortHash {} could not be found or wrong poc", shortHashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.UPDATE_NOT_FOUND);
        }
        addStatistics(quickTest);
        try {
            quickTestArchiveRepository.save(mappingQuickTestToQuickTestAchive(quickTest, pdf));
            log.debug("New QuickTestArchive created for poc {} and shortHashedGuid {}",
                    quickTest.getPocId(), shortHashedGuid);
        } catch (IOException e) {
            log.error("Could not read pdf. createNewQuickTestArchive failed. IO Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INTERNAL_ERROR);
        }
        try {
            quickTestRepository.deleteById(quickTest.getHashedGuid());
            log.debug("QuickTest moved to QuickTestArchive for poc {} and shortHashedGuid {}",
                    quickTest.getPocId(), shortHashedGuid);
        } catch (Exception e) {
            log.error("createNewQuickTestArchive failed. Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INTERNAL_ERROR);
        }
    }

    @Transactional
    protected void addStatistics(QuickTest quickTest) {
        if (quickTestStatisticsRepository.findByPocIdAndDate(quickTest.getPocId(), LocalDate.now()) == null) {
            quickTestStatisticsRepository.save(new QuickTestStatistics(quickTest.getPocId(), quickTest.getTenantId()));
            log.debug("New QuickTestStatistics created for poc {}", quickTest.getPocId());
        }
        if (quickTest.getTestResult() == 7) {
            quickTestStatisticsRepository.incrementPositiveAndTotalTestCount(quickTest.getPocId(), LocalDate.now());
        } else {
            quickTestStatisticsRepository.incrementTotalTestCount(quickTest.getPocId(), LocalDate.now());
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
