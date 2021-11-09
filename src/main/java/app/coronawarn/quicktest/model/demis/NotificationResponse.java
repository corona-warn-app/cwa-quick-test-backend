package app.coronawarn.quicktest.model.demis;

import lombok.Data;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;

@Data
public class NotificationResponse {

    private OperationOutcome operationOutcome;
    private Bundle resultBundle;
    private DemisStatus status;

}
