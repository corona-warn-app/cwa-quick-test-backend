package app.coronawarn.quicktest.model.demis;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

@ResourceDef(name = "Composition", profile = "https://demis.rki.de/fhir/StructureDefinition/NotificationLaboratory")
public class NotificationLaboratory extends Notification {

    /**
     * Notification of a laboratory.
     */
    public NotificationLaboratory() {
        super();
        this.addCategory(
          new CodeableConcept().addCoding(
            new Coding().setSystem("http://loinc.org").setCode("11502-2").setDisplay("Laboratory report")));
    }

}
