package app.coronawarn.quicktest.migration.v001tov002.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestArchiveMigrationV001;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestArchiveRepositoryMigrationV001 extends JpaRepository<QuickTestArchiveMigrationV001, String> {

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
