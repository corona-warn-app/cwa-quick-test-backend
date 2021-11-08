package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;

@ResourceDef(name = "PractitionerRole", profile = NotifierRole.PROFILE)
public class NotifierRole extends PractitionerRole {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/NotifierRole";

    public NotifierRole() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
    }

    /**
     * Create NotifierRole with reference to a notifying facility.
     * @param organization the reference
     * @return the NotifierRole
     */
    public NotifierRole withOrganization(Organization organization) {
        this.setOrganization(DemisUtils.createResourceReference(organization));
        return this;
    }
}
