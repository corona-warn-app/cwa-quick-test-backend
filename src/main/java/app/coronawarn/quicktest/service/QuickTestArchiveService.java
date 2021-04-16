package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestArchiveService {

    private final QuickTestArchiveRepository quickTestArchiveRepository;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param hashedGuid to identify quicktest
     * @return PDF as byte array
     * @throws QuickTestServiceException if quicktest not found.
     */
    public byte[] getPdf(String hashedGuid)
        throws QuickTestServiceException {
        QuickTestArchive quickTestArchive = quickTestArchiveRepository.findById(hashedGuid).get();
        if (quickTestArchive == null) {
            log.info("Requested Quick Test with HashedGuid {} could not be found or wrong poc", hashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.NOT_FOUND);
        }
        return quickTestArchive.getPdf();
    }


}
