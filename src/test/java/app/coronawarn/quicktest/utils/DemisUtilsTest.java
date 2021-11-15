package app.coronawarn.quicktest.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import app.coronawarn.quicktest.domain.QuickTest;
import java.time.LocalDate;
import java.util.List;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class DemisUtilsTest {

    @Test
    void generateUuid() {
        assertThat(DemisUtils.createId()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    }

    @Test
    void createValidResourceReference() {
        Patient input = new Patient();
        input.setId("uuid");

        assertThat(DemisUtils.createResourceReference(input).getReference()).isEqualTo("urn:uuid:"+ input.getId());
    }

    @Test
    void createResourceReferenceFromNullThrowsException() {
        assertThatThrownBy(() -> DemisUtils.createResourceReference(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Resource for Reference is null");
    }

    @Test
    void createMetaInformation() {
        String profile = "fhir-profile";
        assertThat(DemisUtils.createMetaInformation(profile))
          .isInstanceOf(Meta.class)
          .satisfies(
            meta -> assertThat(meta.hasProfile(profile)).isTrue());
    }

    @Test
    void parseBirthdaySuccess() {
        assertThat(DemisUtils.parseBirthday("1983-10-15")).hasYear(1983).hasMonth(10).hasDayOfMonth(15);
    }

    @Test
    void parseBirthdayFailureToDefaultDate() {
        LocalDate now = LocalDate.now();
        assertThat(DemisUtils.parseBirthday("1983.15.10"))
          .hasYear(now.getYear())
          .hasMonth(now.getMonthValue())
          .hasDayOfMonth(now.getDayOfMonth());
    }

    @Test
    void createAddressFromQuicktest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setCity("testcity");
        quickTest.setZipCode("10115");
        quickTest.setStreet("teststreet 8");

        assertThat(DemisUtils.createAddress(quickTest)).satisfies(address -> {
            assertThat(address.getCity()).isEqualTo(quickTest.getCity());
            assertThat(address.getPostalCode()).isEqualTo(quickTest.getZipCode());
            assertThat(address.hasLine(quickTest.getStreet())).isTrue();
            assertThat(address.getCountry()).isEqualTo("20422");
        });
    }

    @Test
    void createAddressFromPocInformation() {
        List<String> pocInformation = List.of("testcity", "teststreet 8", "10115 Berlin");

        assertThat(DemisUtils.createAddress(pocInformation)).hasValueSatisfying(address -> {
            assertThat(address.getCity()).isEqualTo("Berlin");
            assertThat(address.getPostalCode()).isEqualTo("10115");
            assertThat(address.hasLine("teststreet 8")).isTrue();
            assertThat(address.getCountry()).isEqualTo("20422");
        });
    }

    @Test
    void noAddressFromMalformedPocInformation() {
        List<String> pocInformation = List.of("testcity", "teststreet 8", "Berlin");

        assertThat(DemisUtils.createAddress(pocInformation)).isEmpty();
    }
}