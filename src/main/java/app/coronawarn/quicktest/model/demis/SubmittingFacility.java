package app.coronawarn.quicktest.model.demis;

import static app.coronawarn.quicktest.utils.DemisUtils.BSNR_SYSTEM;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

@ResourceDef(name = "Organization", profile = SubmittingFacility.PROFILE)
public class SubmittingFacility extends Organization {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/SubmittingFacility";

    public SubmittingFacility() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
    }

    /**
     * Create a SubmittingFacility with name.
     * @param name the name of the facility
     */
    public SubmittingFacility withName(final String name) {
        this.setName(name);
        return this;
    }

    public SubmittingFacility withIdentifier(final String value) {
        this.addIdentifier(new Identifier().setSystem(BSNR_SYSTEM).setValue(value));
        return this;
    }

    public SubmittingFacility withAddress(final Address address) {
        this.addAddress(address);
        return this;
    }

    /**
     * Add contact.
     * @param value data
     * @return current object
     */
    public SubmittingFacility withContact(final String value) {
        this.addTelecom(
          new ContactPoint()
            .setSystem(ContactPoint.ContactPointSystem.URL)
            .setUse(ContactPoint.ContactPointUse.TEMP)
            .setValue(value)
        );
        return this;
    }

}
