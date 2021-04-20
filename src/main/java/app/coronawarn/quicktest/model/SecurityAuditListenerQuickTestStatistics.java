package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import javax.persistence.PostLoad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties("auditlogs")
public class SecurityAuditListenerQuickTestStatistics {

    @Autowired
    private QuickTestConfig quickTestConfig;
    @Autowired
    private Utilities utilities;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; pocName {}; action: {}; Object: {}; ID: {}";

    @PostLoad
    private void afterSelectQuickTest(QuickTestStatistics quickTestStatistics) throws QuickTestServiceException {
        log.info(pattern,
                utilities.getUserNameFromToken(),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey()),
                utilities.getIdsFromToken().get(quickTestConfig.getPointOfCareIdName()),
                "Select",
                "QuickTestStatistics",
                quickTestStatistics.getId());
    }
}
