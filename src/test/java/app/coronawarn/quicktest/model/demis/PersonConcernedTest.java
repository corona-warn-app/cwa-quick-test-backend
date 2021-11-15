package app.coronawarn.quicktest.model.demis;

import static app.coronawarn.quicktest.utils.QuicktestUtils.getQuickTest;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.quicktest.domain.QuickTest;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PersonConcernedTest {


    @Test
    void createPersonConcerned() {
        QuickTest quickTest = getQuickTest();
        PersonConcerned personConcerned = new PersonConcerned().fromQuicktest(quickTest);

        assertThat(personConcerned).satisfies(created -> {
            assertThat(created.getId()).isNotEmpty();
            assertThat(created.getName())
              .filteredOn(humanName -> humanName.getGiven().stream().anyMatch(given -> given.getValue().equals(quickTest.getFirstName())))
              .extracting("family").containsExactly(quickTest.getLastName());
            assertThat(created.getGender()).isEqualTo(Enumerations.AdministrativeGender.OTHER);
            assertThat(created.getBirthDate()).hasYear(1911).hasMonth(11).hasDayOfMonth(11);
            assertThat(created.getTelecom()).isNotEmpty()
              .filteredOn(tel -> tel.getSystem().equals(ContactPoint.ContactPointSystem.PHONE))
              .extracting("value").containsExactly(quickTest.getPhoneNumber());
            assertThat(created.getTelecom())
              .filteredOn(tel -> tel.getSystem().equals(ContactPoint.ContactPointSystem.EMAIL))
              .extracting("value").containsExactly(quickTest.getEmail());
        });
    }

}