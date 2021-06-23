package app.coronawarn.quicktest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@SpringBootTest
public class QuickTestArchiveServiceTest {

    QuickTestArchive quickTestArchive;
    @Mock
    private QuickTestConfig quickTestConfig;
    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;
    @InjectMocks
    private QuickTestArchiveService quickTestArchiveService;

    @BeforeEach
    void setUp() {
        quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTestArchive.setShortHashedGuid("6fa4dcec");
        quickTestArchive.setTenantId("tenant");
        quickTestArchive.setCreatedAt(LocalDateTime.now());
        quickTestArchive.setUpdatedAt(LocalDateTime.now());
        quickTestArchive.setConfirmationCwa(true);
        quickTestArchive.setTestResult((short) 6);
        quickTestArchive.setPrivacyAgreement(true);
        quickTestArchive.setLastName("1");
        quickTestArchive.setFirstName("1");
        quickTestArchive.setEmail("v@e.o");
        quickTestArchive.setPhoneNumber("+490000");
        quickTestArchive.setSex(Sex.DIVERSE);
        quickTestArchive.setStreet("f");
        quickTestArchive.setHouseNumber("a");
        quickTestArchive.setZipCode("11111");
        quickTestArchive.setCity("f");
        quickTestArchive.setTestBrandId("testbrand");
        quickTestArchive.setTestBrandName("brandname");
        quickTestArchive.setBirthday(LocalDate.now().toString());
        quickTestArchive.setPdf("test output".getBytes());
    }

    @Test
    void createNewQuickTestArchiveQuickTest() {
        when(quickTestArchiveRepository.findByHashedGuid(any())).thenReturn(Optional.of(quickTestArchive));
        assertEquals(quickTestArchive.getPdf(), quickTestArchiveService.getPdf("sgserh"));
    }

    @Test
    void createNewQuickTestArchiveQuickTestNotFound() {
        when(quickTestArchiveRepository.findByHashedGuid(any())).thenReturn(Optional.empty());
        try {
            quickTestArchiveService.getPdf("sgserh");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.NOT_FOUND, "wrong status");
        }
    }

    @Test
    void findByTestResultAndUpdatedAtBetweenTest() {
        when(quickTestArchiveRepository.findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(quickTestArchive));
        when(quickTestArchiveRepository.findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any())).thenReturn(Collections.singletonList(quickTestArchive));
        List<QuickTestArchive> quickTestArchives =
            quickTestArchiveService.findByTestResultAndUpdatedAtBetween(
                new HashMap<>(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        assertEquals(1, quickTestArchives.size());
        verify(quickTestArchiveRepository, times(1))
            .findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any());
        verify(quickTestArchiveRepository, times(0)).findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any());
        checkResponse(quickTestArchive,
            quickTestArchives.get(0));

        quickTestArchives =
            quickTestArchiveService.findByTestResultAndUpdatedAtBetween(
                new HashMap<>(),
                (short) 5,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        assertEquals(1, quickTestArchives.size());
        verify(quickTestArchiveRepository, times(1))
            .findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any());
        verify(quickTestArchiveRepository, times(1)).findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any());
        checkResponse(quickTestArchive,
            quickTestArchives.get(0));

    }

    @Test
    void findUnsentPositiveTestsTest() {

    }


    private void checkResponse(QuickTestArchive expected, QuickTestArchive act) {
        assertEquals(expected.getHashedGuid(), act.getHashedGuid());
        assertEquals(expected.getLastName(), act.getLastName());
        assertEquals(expected.getFirstName(), act.getFirstName());
        assertEquals(expected.getEmail(), act.getEmail());
        assertEquals(expected.getPhoneNumber(), act.getPhoneNumber());
        assertEquals(expected.getSex(), act.getSex());
        assertEquals(expected.getStreet(), act.getStreet());
        assertEquals(expected.getHouseNumber(), act.getHouseNumber());
        assertEquals(expected.getZipCode(), act.getZipCode());
        assertEquals(expected.getCity(), act.getCity());
        assertEquals(expected.getBirthday(), act.getBirthday());
        assertEquals(expected.getTestResult(), act.getTestResult());
    }
}

