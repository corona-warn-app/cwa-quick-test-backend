package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestArchiveRepository extends JpaRepository<QuickTestArchive, String> {
}
