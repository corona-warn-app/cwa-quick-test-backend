package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.Date;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;

@ResourceDef(name = "Composition", profile = NotificationSarsCov2.PROFILE)
public class NotificationSarsCov2 extends NotificationLaboratory {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/NotificationLaboratorySARSCoV2";

    /**
     * The Composition / Notification.
     */
    public NotificationSarsCov2() {
        super();
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
        this.setStatus(Composition.CompositionStatus.FINAL);
        this.setTitle("SARS-CoV-2 Schnelltestmeldung");
        this.setDate(new Date());
    }

    public NotificationSarsCov2 withSubject(Patient subject) {
        this.setSubject(DemisUtils.createResourceReference(subject));
        return this;
    }

    public NotificationSarsCov2 withAuthor(PractitionerRole author) {
        this.addAuthor(DemisUtils.createResourceReference(author));
        return this;
    }

    /**
     * NotificationSarsCov2 with a Condition.
     * @param condition the condition
     * @return NotificationSarsCov2
     */
    public NotificationSarsCov2 withCondition(Condition condition) {
        CodeableConcept diagnosisConcept = new CodeableConcept();
        diagnosisConcept.addCoding()
          .setSystem("http://loinc.org")
          .setCode("29308-4")
          .setDisplay("Diagnosis");

        this.addSection().setCode(diagnosisConcept).addEntry(DemisUtils.createResourceReference(condition));
        return this;
    }

    /**
     * NotificationSarsCov2 with an Observation.
     * @param observation the observation
     * @return the NotificationSarsCov2
     */
    public NotificationSarsCov2 withObservation(Observation observation) {
        CodeableConcept observationConcept = new CodeableConcept();
        observationConcept.addCoding()
          .setSystem("http://loinc.org")
          .setCode("18725-2")
          .setDisplay("Microbiology studies (set)");

        this.addSection().setCode(observationConcept).addEntry(DemisUtils.createResourceReference(observation));
        return this;
    }
}
