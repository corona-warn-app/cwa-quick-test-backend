package app.coronawarn.quicktest.model.demis;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.Date;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;

@ResourceDef(name = "Patient", profile = PersonConcerned.PROFILE)
public class PersonConcerned extends Patient {

    public static final String PROFILE = "https://demis.rki.de/fhir/StructureDefinition/NotifiedPerson";

    public PersonConcerned() {
        this.setId(DemisUtils.createId());
        this.setMeta(DemisUtils.createMetaInformation(PROFILE));
    }

    public PersonConcerned withName(String first, String last) {
        this.addName().setFamily(last).addGiven(first);
        return this;
    }

    public PersonConcerned withGender(Sex sex) {
        this.setGender(getGender(sex));
        return this;
    }

    public PersonConcerned withAddress(Address address) {
        this.addAddress(address);
        return this;
    }

    public PersonConcerned withBirthday(Date birthday) {
        this.setBirthDate(birthday);
        return this;
    }

    /**
     * Create a PersonConcerned from Quickest information.
     * @param quickTest the quicktest
     * @return the PersonConcerned
     */
    public PersonConcerned fromQuicktest(QuickTest quickTest) {
        return this
          .withName(quickTest.getFirstName(), quickTest.getLastName())
          .withGender(quickTest.getSex())
          .withBirthday(DemisUtils.parseBirthday(quickTest.getBirthday()))
          .withAddress(DemisUtils.createAddress(quickTest));
    }

    private Enumerations.AdministrativeGender getGender(Sex sex) {
        Enumerations.AdministrativeGender gender;
        switch (sex) {
          case MALE:
              gender = Enumerations.AdministrativeGender.MALE;
              break;
          case FEMALE:
              gender = Enumerations.AdministrativeGender.FEMALE;
              break;
          case DIVERSE:
              gender =  Enumerations.AdministrativeGender.OTHER;
              break;
          default:
              gender = Enumerations.AdministrativeGender.NULL;
        }
        return gender;
    }
}
