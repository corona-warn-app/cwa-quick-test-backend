package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
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
public class SecurityAuditListenerQuickTestArchive {

    private final Utilities utilities;
    private final QuickTestConfig quickTestConfig;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTestArchive(QuickTestArchive quickTestArchive) throws ResponseStatusException {
        createAuditLog(quickTestArchive, "Select");
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTestArchive(QuickTestArchive quickTestArchive) throws ResponseStatusException {
        createAuditLog(quickTestArchive, "Save");
    }

    private void createAuditLog(QuickTestArchive quickTestArchive, String action) {
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
