package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.model.demis.DemisStatus;

public interface QuickTestArchiveView {
    String getHashedGuid();

    DemisStatus getDemisStatus();
}
