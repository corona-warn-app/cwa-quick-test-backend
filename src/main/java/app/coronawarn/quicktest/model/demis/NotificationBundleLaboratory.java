package app.coronawarn.quicktest.model.demis;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumeration;

@ResourceDef(name = "Bundle", profile = "https://demis.rki.de/fhir/StructureDefinition/NotificationBundleLaboratory")
public class NotificationBundleLaboratory extends NotificationBundle {

    public NotificationBundleLaboratory() {
        this.setType(Bundle.BundleType.DOCUMENT);
    }

    public NotificationBundleLaboratory(final Enumeration<Bundle.BundleType> type) {
        super(type);
        this.setType(Bundle.BundleType.DOCUMENT);
    }
}
