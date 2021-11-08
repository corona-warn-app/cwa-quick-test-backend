package app.coronawarn.quicktest.model.demis;

import static app.coronawarn.quicktest.utils.DemisUtils.BSNR_SYSTEM;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

@ResourceDef(name = "Organization", profile = NotifierFacility.PROFILE)
public class NotifierFacility extends Organization {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/NotifierFacility";

    /**
     * Create a NotifierFacility.
     */
    public NotifierFacility() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));

        CodeableConcept code = new CodeableConcept();
        code.addCoding()
          .setCode("laboratory")
          .setDisplay("Erregerdiagnostische Untersuchungsstelle")
          .setSystem("https://demis.rki.de/fhir/CodeSystem/organizationType");
        this.addType(code);
    }

    /**
     * Create a NotifierFacility with name.
     * @param name the name of the facility
     */
    public NotifierFacility withName(final String name) {
        this.setName(name);
        return this;
    }

    /**
     * with id.
     * @param value the id
     * @return notifier facility
     */
    public NotifierFacility withIdentifier(final String value) {
        this.addIdentifier(new Identifier().setSystem(BSNR_SYSTEM).setValue(value));
        return this;
    }

    public NotifierFacility withAddress(final Address address) {
        this.addAddress(address);
        return this;
    }

    /**
     * Add contact.
     * @param value data
     * @return current object
     */
    public NotifierFacility withContact(final String value) {
        this.addTelecom(
          new ContactPoint()
            .setSystem(ContactPoint.ContactPointSystem.URL)
            .setUse(ContactPoint.ContactPointUse.TEMP)
            .setValue(value)
        );
        return this;
    }

}
