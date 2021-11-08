package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.Date;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;

@ResourceDef(name = "Specimen", profile = SpecimenSarsCoV2.PROFILE)
public class SpecimenSarsCoV2 extends Specimen {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/SpecimenSARSCoV2";

    /**
     * Specimen.
     */
    public SpecimenSarsCoV2() {
        this.setId(DemisUtils.createId());
        this.setStatus(Specimen.SpecimenStatus.AVAILABLE);
        this.setType(new CodeableConcept().setText("Tupferabstrich"));
        this.setReceivedTime(new Date());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
    }

    /**
     * with subect ref.
     * @param subject patient
     * @return specimen
     */
    public SpecimenSarsCoV2 withSubject(Patient subject) {
        this.setSubject(DemisUtils.createResourceReference(subject));
        return this;
    }

    /**
     * with collector ref.
     * @param collector collector
     * @return specimen
     */
    public SpecimenSarsCoV2 withCollector(SubmittingRole collector) {
        this.setCollection(
          new SpecimenCollectionComponent().setCollector(
            DemisUtils.createResourceReference(collector)
          )
        );
        return this;
    }
}
