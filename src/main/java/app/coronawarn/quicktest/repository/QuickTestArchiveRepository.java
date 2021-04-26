package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestArchiveRepository extends JpaRepository<QuickTestArchive, String> {

    Optional<QuickTestArchive> findByHashedGuid(String hashedGuid);

    List<QuickTestArchive> findAllByTenantIdAndPocIdAndUpdatedAtBetween(
        String tenantId,
        String pocId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
    );

    List<QuickTestArchive> findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(
            String tenantId,
            String pocId,
            Short testResult,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    );

}
