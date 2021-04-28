package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestArchiveService {

    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final QuickTestConfig quickTestConfig;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param hashedGuid to identify quicktest
     * @return PDF as byte array
     * @throws ResponseStatusException if quicktest not found.
     */
    public byte[] getPdf(String hashedGuid)
        throws ResponseStatusException {
        Optional<QuickTestArchive> quickTestArchive = quickTestArchiveRepository.findByHashedGuid(hashedGuid);
        if (quickTestArchive.isEmpty()) {
            log.info("Requested Quick Test with HashedGuid {} could not be found or wrong poc", hashedGuid);
            log.debug("Requested Quick Test with HashedGuid could not be found or wrong poc");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return quickTestArchive.get().getPdf();
    }

    /**
     * Finds all quicktests in archive table by test result and time range.
     *
     * @param testResult test result value (5...9) or null
     * @param dateFrom   Start date
     * @param dateTo     End date
     * @return quickTestArchives List of all found quickTestArchives
     */
    public List<QuickTestArchive> findByTestResultAndUpdatedAtBetween(
        Map<String, String> ids, Short testResult, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<QuickTestArchive> archives;
        if (testResult == null) {
            archives = quickTestArchiveRepository.findAllByTenantIdAndPocIdAndUpdatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                dateFrom,
                dateTo);
        } else {
            archives = quickTestArchiveRepository.findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                testResult,
                dateFrom,
                dateTo);
        }
        return archives;
    }

}
