package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class QuickTestArchiveServiceTest {

    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;

    @InjectMocks
    private QuickTestArchiveService quickTestArchiveService;

    @Test // TODO FIXME
    void createNewQuickTestArchiveQuickTestNotFound() {
        when(quickTestArchiveRepository.findById("sgserh")).thenReturn(null);
        try {
            quickTestArchiveService.getPdf("sgserh");
            fail("has to throw exception");
        } catch (QuickTestServiceException e) {
            assertTrue(e.getReason().equals(QuickTestServiceException.Reason.NOT_FOUND), "wrong exception");
        }
    }
}

