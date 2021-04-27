package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
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
public class SecurityAuditListenerQuickTest {

    private final QuickTestConfig quickTestConfig;
    private final Utilities utilities;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Select");
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Save");
    }

    @PostRemove
    private void afterRemoveQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, "Remove");
    }

    private void createAuditLog(QuickTest quickTest, String action) {
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
