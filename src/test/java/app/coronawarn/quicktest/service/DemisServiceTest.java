package app.coronawarn.quicktest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.client.DemisServerClient;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.demis.DemisStatus;
import app.coronawarn.quicktest.model.demis.NotificationBundleLaboratory;
import app.coronawarn.quicktest.model.demis.NotificationResponse;
import app.coronawarn.quicktest.model.demis.NotifierFacility;
import app.coronawarn.quicktest.model.demis.NotifierRole;
import app.coronawarn.quicktest.model.demis.PersonConcerned;
import app.coronawarn.quicktest.model.demis.SpecimenSarsCoV2;
import app.coronawarn.quicktest.model.demis.SubmittingFacility;
import app.coronawarn.quicktest.model.demis.SubmittingRole;
import app.coronawarn.quicktest.utils.QuicktestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@Slf4j
@ExtendWith(MockitoExtension.class)
class DemisServiceTest {

    @Mock
    private DemisServerClient demisServerClient;

    @InjectMocks
    private DemisService underTest;

    @Captor
    ArgumentCaptor<NotificationBundleLaboratory> notificationCaptor;

    @Test
    void failOnWrongTestResult() {
        assertThatThrownBy(() -> underTest.handlePositiveTest(
          getQuickTest(6), new ArrayList<>(), Optional.empty()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Can not create Notification for non-positive test.");
    }

    @Test
    void failOnMalformedPocInformation() {

        assertThat(underTest.handlePositiveTest(
          getQuickTest(7), getPocInformation(false), Optional.empty()))
          .satisfies(result -> {
             assertThat(result.getDemisStatus()).isEqualTo(DemisStatus.INVALID_INPUT);
             assertThat(result.getDetails()).contains("Could not create valid address from token.");
          });
    }

    @Test
    void createFhirBundle() {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setStatus(DemisStatus.NONE);
        when(demisServerClient.sendNotification(any(NotificationBundleLaboratory.class)))
          .thenReturn(notificationResponse);

        underTest.handlePositiveTest(getQuickTest(7), getPocInformation(true), Optional.empty());

        verify(demisServerClient).sendNotification(notificationCaptor.capture());
        verifyNoMoreInteractions(demisServerClient);

        assertThat(notificationCaptor.getValue()).satisfies(bundle -> {
            assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.DOCUMENT);
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof PersonConcerned).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof NotifierFacility).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof NotifierRole).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof SubmittingFacility).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof SubmittingRole).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof Condition).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof SpecimenSarsCoV2).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof Observation).isNotEmpty();
            assertThat(bundle.getEntry()).filteredOn(entry -> entry.getResource() instanceof Composition).isNotEmpty();
        });
    }


    private List<String> getPocInformation(boolean valid) {
        return List.of("Testcenter 1", "Teststreet 2", valid ? "10115 Berlin" : "Berlin");
    }

    private QuickTest getQuickTest(int testResult) {
        QuickTest quicktest = QuicktestUtils.getQuickTest();
        quicktest.setTestResult((short) testResult);
        return quicktest;
    }
}