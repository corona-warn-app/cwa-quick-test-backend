package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestArchiveRepository extends JpaRepository<QuickTestArchive, String> {

    Optional<QuickTestArchive> findByHashedGuid(String hashedGuid);

    List<QuickTestArchive> findAllByUpdatedAtBetween(
        LocalDateTime dateFrom,
        LocalDateTime dateTo
    );

    List<QuickTestArchive> findAllByTestResultAndUpdatedAtBetween(
            Short testResult,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    );

}
