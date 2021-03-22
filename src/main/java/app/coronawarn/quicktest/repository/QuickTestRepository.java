package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickTestRepository extends JpaRepository<QuickTest, Long> {

}
