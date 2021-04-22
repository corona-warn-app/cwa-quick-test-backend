package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties("auditlogs")
public class SecurityAuditListenerQuickTest {

    private final QuickTestConfig quickTestConfig;
    private final Utilities utilities;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTest(QuickTest quickTest) throws QuickTestServiceException {
        log.info(pattern,
                utilities.getUserNameFromToken(),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey()),
                "Select",
                "QuickTest",
                quickTest.getHashedGuid());
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTest(QuickTest quickTest) throws QuickTestServiceException {
        log.info(pattern,
                utilities.getUserNameFromToken(),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey()),
                "Save",
                "QuickTest",
                quickTest.getHashedGuid());
    }
}
