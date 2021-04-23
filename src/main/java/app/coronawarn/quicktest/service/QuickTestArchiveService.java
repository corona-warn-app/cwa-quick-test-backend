package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestArchiveService {

    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final ModelMapper modelMapper;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param hashedGuid to identify quicktest
     * @return PDF as byte array
     * @throws QuickTestServiceException if quicktest not found.
     */
    public byte[] getPdf(String hashedGuid)
        throws QuickTestServiceException {
        Optional<QuickTestArchive> quickTestArchive = quickTestArchiveRepository.findByHashedGuid(hashedGuid);
        if (quickTestArchive.isEmpty()) {
            log.info("Requested Quick Test with HashedGuid {} could not be found or wrong poc", hashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.NOT_FOUND);
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
        Short testResult, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<QuickTestArchive> archives;
        if (testResult == null) {
            archives = quickTestArchiveRepository.findAllByUpdatedAtBetween(
                dateFrom, dateTo);
        } else {
            archives = quickTestArchiveRepository.findAllByTestResultAndUpdatedAtBetween(
                testResult, dateFrom, dateTo);
        }
        return archives;
    }

}
