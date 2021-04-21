package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
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
public class SecurityAuditListenerQuickTestArchive {

    private final Utilities utilities;
    private final QuickTestConfig quickTestConfig;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; pocName {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTestArchive(QuickTestArchive quickTestArchive) throws QuickTestServiceException {
        log.info(pattern,
                utilities.getUserNameFromToken(),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getPointOfCareIdName()),
                "Select",
                "QuickTestArchive",
                quickTestArchive.getHashedGuid());
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTestArchive(QuickTestArchive quickTestArchive) throws QuickTestServiceException {
        log.info(pattern,
                utilities.getUserNameFromToken(),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getPointOfCareIdName()),
                "Save",
                "QuickTestArchive",
                quickTestArchive.getHashedGuid());
    }
}
