package app.coronawarn.quicktest.model.demis;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Condition;

@ResourceDef(name = "Condition", profile = "https://demis.rki.de/fhir/StructureDefinition/Diagnose")
public class Diagnose extends Condition {
}
