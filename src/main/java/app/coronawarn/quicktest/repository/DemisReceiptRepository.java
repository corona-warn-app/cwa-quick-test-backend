package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.DemisReceipt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemisReceiptRepository extends JpaRepository<DemisReceipt, String> {

    Optional<DemisReceipt> findByHashedGuid(String hashedGuid);
}
