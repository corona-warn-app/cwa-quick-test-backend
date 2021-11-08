package app.coronawarn.quicktest.model.demis;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;

@ResourceDef(name = "Composition", profile = "https://demis.rki.de/fhir/StructureDefinition/Notification")
public class Notification extends Composition {

    /**
     * Notification base.
     */
    public Notification() {
        this.setType(new CodeableConcept().addCoding(
            new Coding().setSystem("http://loinc.org").setCode("34782-3").setDisplay("Infectious disease Note")));
    }
}
