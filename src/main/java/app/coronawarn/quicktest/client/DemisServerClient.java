package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.demis.NotificationResponse;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.RequestBody;

public interface DemisServerClient {

    NotificationResponse sendNotification(@RequestBody Bundle notification);
}
