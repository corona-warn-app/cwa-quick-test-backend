package app.coronawarn.quicktest.migration.v001tov002.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestArchiveMigrationV001;
import app.coronawarn.quicktest.utils.Utilities;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
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
public class SecurityAuditListenerQuickTestArchiveMigrationV001 {

    private final Utilities utilities;
    private final QuickTestConfig quickTestConfig;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTestArchive(QuickTestArchiveMigrationV001 quickTestArchive)
        throws ResponseStatusException {
        createAuditLog(quickTestArchive, "Select");
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTestArchive(QuickTestArchiveMigrationV001 quickTestArchive)
        throws ResponseStatusException {
        createAuditLog(quickTestArchive, "Save");
    }

    private void createAuditLog(QuickTestArchiveMigrationV001 quickTestArchive, String action) {
        String name;
        String tenantId;
        String pocId;

        try {
            name = utilities.getUserNameFromToken();
            tenantId = utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey());
            pocId = utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey());
        } catch (ResponseStatusException e) {
            name = "called by Backend";
            tenantId = quickTestArchive.getTenantId();
            pocId = quickTestArchive.getPocId();
        }

        log.info(pattern,
            name,
            tenantId,
            pocId,
            action,
            "QuickTestArchive",
            quickTestArchive.getHashedGuid());
    }
}
