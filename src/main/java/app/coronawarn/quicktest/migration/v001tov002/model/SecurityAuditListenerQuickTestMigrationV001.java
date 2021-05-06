package app.coronawarn.quicktest.migration.v001tov002.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestMigrationV001;
import app.coronawarn.quicktest.utils.Utilities;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties("auditlogs")
public class SecurityAuditListenerQuickTestMigrationV001 {

    private final QuickTestConfig quickTestConfig;
    private final Utilities utilities;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTest(QuickTestMigrationV001 quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Select");
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTest(QuickTestMigrationV001 quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Save");
    }

    @PostRemove
    private void afterRemoveQuickTest(QuickTestMigrationV001 quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Remove");
    }

    private void createAuditLog(QuickTestMigrationV001 quickTest, String action) {
        String name;
        String tenantId;
        String pocId;

        try {
            name = utilities.getUserNameFromToken();
            tenantId = utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey());
            pocId = utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey());
        } catch (ResponseStatusException e) {
            name = "called by Backend";
            tenantId = quickTest.getTenantId();
            pocId = quickTest.getPocId();
        }

        log.info(pattern,
            name,
            tenantId,
            pocId,
            action,
            "QuickTest",
            quickTest.getHashedGuid());
    }

}
