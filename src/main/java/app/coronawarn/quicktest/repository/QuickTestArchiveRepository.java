package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestArchiveRepository extends JpaRepository<QuickTestArchive, String> {

    Optional<QuickTestArchive> findByHashedGuid(String hashedGuid);
}
