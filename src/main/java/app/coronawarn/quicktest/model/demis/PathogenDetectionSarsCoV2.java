package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;


@ResourceDef(name = "Observation", profile = PathogenDetectionSarsCoV2.PROFILE)
public class PathogenDetectionSarsCoV2 extends Observation {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/PathogenDetectionSARSCoV2";
    public static final String POSITIVE = "P O S I T I V";

    /**
     * Observation of a positive SarsCov2 specimen.
     */
    public PathogenDetectionSarsCoV2() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
        this.setStatus(Observation.ObservationStatus.FINAL);
        this.setValue(new StringType(POSITIVE));

        this.setLaboratoryCategory();
        this.setSarsCoV2Code();
        this.setPosInterpretation();
        this.setSarsCov2Method();
    }

    public PathogenDetectionSarsCoV2 withSubject(Patient patient) {
        this.setSubject(DemisUtils.createResourceReference(patient));
        return this;
    }

    public PathogenDetectionSarsCoV2 withSpecimen(SpecimenSarsCoV2 specimen) {
        this.setSpecimen(DemisUtils.createResourceReference(specimen));
        return this;
    }

    private void setLaboratoryCategory() {
        CodeableConcept category = new CodeableConcept();
        category.addCoding()
          .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
          .setCode("laboratory");

        this.addCategory(category);
    }

    private void setSarsCoV2Code() {
        this.setCode(new CodeableConcept(
          new Coding().setSystem("http://loinc.org").setCode("94660-8").setDisplay("SARS-CoV-2 RNA NAA+probe Ql")));
    }

    private void setPosInterpretation() {
        CodeableConcept interpretation = new CodeableConcept();
        interpretation.addCoding()
          .setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation")
          .setCode("POS");
        this.addInterpretation(interpretation);
    }

    private void setSarsCov2Method() {
        CodeableConcept method = new CodeableConcept();
        method.setText("Coronavirus SARS-CoV-2 (RAT)");
        this.setMethod(method);
    }
}
