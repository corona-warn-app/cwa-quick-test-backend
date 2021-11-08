package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;

@ResourceDef(name = "Condition", profile = DiagnoseSarsCoV2.PROFILE)
public class DiagnoseSarsCoV2 extends Diagnose {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/DiagnoseSARSCoV2";

    public DiagnoseSarsCoV2() {
        this.setSarsCoV2Code();
    }

    /**
     * Condition with subject reference.
     *
     * @param subject the patient.
     * @return the condition.
     */
    public DiagnoseSarsCoV2 withSubject(Patient subject) {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
        this.setSubject(DemisUtils.createResourceReference(subject));
        return this;
    }

    private void setSarsCoV2Code() {
        this.setCode(new CodeableConcept(
          new Coding().setSystem("http://fhir.de/CodeSystem/dimdi/icd-10-gm").setCode("U07.1!")));
    }
}
