package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;

@ResourceDef(name = "PractitionerRole", profile = SubmittingRole.PROFILE)
public class SubmittingRole extends PractitionerRole {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/SubmittingRole";

    public SubmittingRole() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
    }

    /**
     * Create SubmittingRole with reference to a submitting facility.
     * @param organization the reference
     * @return the SubmittingROle
     */
    public SubmittingRole withOrganization(Organization organization) {
        this.setOrganization(DemisUtils.createResourceReference(organization));
        return this;
    }
}
