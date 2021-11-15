package app.coronawarn.quicktest.service;

import static app.coronawarn.quicktest.utils.DemisUtils.URN;

import app.coronawarn.quicktest.client.DemisServerClient;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.demis.DemisResult;
import app.coronawarn.quicktest.model.demis.DemisStatus;
import app.coronawarn.quicktest.model.demis.DiagnoseSarsCoV2;
import app.coronawarn.quicktest.model.demis.NotificationBundleLaboratory;
import app.coronawarn.quicktest.model.demis.NotificationResponse;
import app.coronawarn.quicktest.model.demis.NotificationSarsCov2;
import app.coronawarn.quicktest.model.demis.NotifierFacility;
import app.coronawarn.quicktest.model.demis.NotifierRole;
import app.coronawarn.quicktest.model.demis.PathogenDetectionSarsCoV2;
import app.coronawarn.quicktest.model.demis.PersonConcerned;
import app.coronawarn.quicktest.model.demis.SpecimenSarsCoV2;
import app.coronawarn.quicktest.model.demis.SubmittingFacility;
import app.coronawarn.quicktest.model.demis.SubmittingRole;
import app.coronawarn.quicktest.utils.DemisUtils;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemisService {

    private final DemisServerClient demisServerClient;

    /**
     * Handle a positive test and send it to Demis.
     * @param quickTest the test
     */
    public DemisResult handlePositiveTest(QuickTest quickTest, List<String> pocInformation, Optional<String> bsnr) {
        if (quickTest.getTestResult() != 7) {
            throw new IllegalArgumentException("Can not create Notification for non-positive test.");
        }

        Address pocAddress = DemisUtils.createAddress(pocInformation)
          .orElseThrow(() -> new IllegalArgumentException("Can not create Address from poc information."));

        NotificationBundleLaboratory payloadBundle = createPayload(quickTest, pocAddress, pocInformation.get(0), bsnr);

        NotificationResponse response = demisServerClient.sendNotification(payloadBundle);
        return handleResponse(response, quickTest);
    }

    private NotificationBundleLaboratory createPayload(QuickTest quickTest, Address pocAddress, String pocName,
                                                       Optional<String> bsnr) {
        final Patient patient = new PersonConcerned().fromQuicktest(quickTest);
        final NotifierFacility notifierFacility = new NotifierFacility().withName(pocName)
          .withAddress(pocAddress).withContact("https://schnelltestportal.de/").withOptionalIdentifier(bsnr);
        final NotifierRole notifierRole = new NotifierRole().withOrganization(notifierFacility);
        final SubmittingFacility submittingFacility = new SubmittingFacility().withName(pocName)
          .withAddress(pocAddress).withContact("https://schnelltestportal.de/").withOptionalIdentifier(bsnr);
        final Condition condition = new DiagnoseSarsCoV2().withSubject(patient);
        final SubmittingRole submittingRole = new SubmittingRole().withOrganization(submittingFacility);
        final SpecimenSarsCoV2 specimen = new SpecimenSarsCoV2().withSubject(patient).withCollector(submittingRole);

        final Observation observation = new PathogenDetectionSarsCoV2().withSubject(patient).withSpecimen(specimen);
        final Composition composition = new NotificationSarsCov2().withSubject(patient).withAuthor(notifierRole)
          .withCondition(condition).withObservation(observation);

        NotificationBundleLaboratory payloadBundle = new NotificationBundleLaboratory();
        payloadBundle.setIdentifier(
          new Identifier().setSystem("http://demis.rki.de/fhir/todo/bundleIdentifier").setValue(DemisUtils.createId()));
        payloadBundle.setTimestamp(new Date());
        payloadBundle.getMeta().setLastUpdated(new Date());
        payloadBundle.addEntry().setResource(composition).setFullUrl(URN + composition.getId());
        payloadBundle.addEntry().setResource(patient).setFullUrl(URN + patient.getId());
        payloadBundle.addEntry().setResource(notifierRole).setFullUrl(URN + notifierRole.getId());
        payloadBundle.addEntry().setResource(notifierFacility).setFullUrl(URN + notifierFacility.getId());
        payloadBundle.addEntry().setResource(submittingRole).setFullUrl(URN + submittingRole.getId());
        payloadBundle.addEntry().setResource(submittingFacility).setFullUrl(URN + submittingFacility.getId());
        payloadBundle.addEntry().setResource(condition).setFullUrl(URN + condition.getId());
        payloadBundle.addEntry().setResource(observation).setFullUrl(URN + observation.getId());
        payloadBundle.addEntry().setResource(specimen).setFullUrl(URN + specimen.getId());
        return payloadBundle;
    }

    private DemisResult handleResponse(NotificationResponse response, QuickTest quickTest) {
        String message = "";
        final DemisStatus status = response.getStatus();
        switch (status) {
          case ZIP_NOT_SUPPORTED:
              message = String.format(
                "Zipcode %s of the concernedPerson is not supported yet for Demis", quickTest.getZipCode());
              break;
          case INVALID_RESPONSE_BODY:
          case SENDING_FAILED:
              message = "Notification could not be sent to Demis";
              break;
          case OK:
              message = String.format(
                "Notification successfully sent and acknowledged: %s", getReceiverInformation(response));
              break;
          case NONE:
              break;
          default:
              throw new IllegalStateException("Unexpected value: " + status);
        }
        log.info(message);
        return DemisResult.builder().demisStatus(status).details(message).build();
    }

    private String getReceiverInformation(NotificationResponse response) {
        return response.getResultBundle().getEntry().stream()
          .filter(it -> it.getResource() instanceof Organization)
          .map(it -> ((Organization) it.getResource()).getName())
          .collect(Collectors.joining(" - "));
    }

}
