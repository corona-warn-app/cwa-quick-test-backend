package app.coronawarn.quicktest.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.client.DccExternalServerClient;
import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.client.VerificationServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.DccPublicKey;
import app.coronawarn.quicktest.model.DccUploadResult;
import app.coronawarn.quicktest.model.RegistrationToken;
import app.coronawarn.quicktest.model.RegistrationTokenRequest;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.dto.DgcData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NonCwaDccServiceTest {

    @Mock
    DccServerClient dccServerClient;
    @Mock
    DccConfig dccConfig;
    @Mock
    QuickTestRepository quickTestRepository;
    @Mock
    QuickTestArchiveRepository quickTestArchiveRepository;
    @Mock
    DgcCryptedPublisher dgcCryptedPublisher;
    @Mock
    QuickTestConfig quickTestConfig;
    @Mock
    PdfGenerator pdfGenerator;
    @Mock
    DgcGenerator dgcGenerator;
    @Mock
    DccExternalServerClient dccExternalServerClient;
    @Mock
    VerificationServerClient verificationServerClient;

    @InjectMocks
    NonCwaDccService underTest;

    private final String hashedGuid = "aac49042c2fc534d6064f732a271e2483e0057685eec196f778ca7d4178918ec";
    private final String sha256 = "6302d6cb7a3a6228ac728242bc7cf4806686d1394cfae98d84506eda4365995c";

    @BeforeEach
    void setUp() {
        when(quickTestConfig.getLabId()).thenReturn("labId4711");
    }

    @Test
    void useHashedId() throws NoSuchAlgorithmException, IOException {
        QuickTest quickTest = createQuickTest();
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid(hashedGuid);

        RegistrationToken regToken = new RegistrationToken();
        regToken.setRegistrationToken("reg");
        when(verificationServerClient.getRegistrationToken(any())).thenReturn(regToken);

        DccPublicKey dccPublicKey = createDccPublicKey(sha256);
        when(dccServerClient.searchPublicKeys(anyString())).thenReturn(List.of(dccPublicKey));

        DccUploadResult dccUploadResult = new DccUploadResult();
        dccUploadResult.setPartialDcc("partial");
        when(dccServerClient.uploadDcc(anyString(), any())).thenReturn(dccUploadResult);

        DgcData dgcData = createDgcData();
        when(dgcCryptedPublisher.createDgc(any(), anyString(), any())).thenReturn(dgcData);

        when(dgcGenerator.dgcSetCosePartial(any(), any())).thenReturn("signed".getBytes());
        when(dgcGenerator.coseToQrCode(any())).thenReturn("qr");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write("pdf".getBytes());
        when(pdfGenerator.appendCertificatePage(any(), any(), anyString())).thenReturn(b);
        when(quickTestArchiveRepository.findByHashedGuid(anyString())).thenReturn(Optional.of(quickTestArchive));

        underTest.createCertificate(quickTest);

        ArgumentCaptor<RegistrationTokenRequest> registrationTokenRequestArgumentCaptor = ArgumentCaptor.forClass(RegistrationTokenRequest.class);
        verify(verificationServerClient).getRegistrationToken(registrationTokenRequestArgumentCaptor.capture());
        assertThat(registrationTokenRequestArgumentCaptor.getValue().getKeyType()).isEqualTo("GUID");
        assertThat(registrationTokenRequestArgumentCaptor.getValue().getKey()).isEqualTo(sha256);
    }

    @Test
    void noPublicKeyOnDcc() {
        QuickTest quickTest = createQuickTest();

        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid(hashedGuid);

        RegistrationToken regToken = new RegistrationToken();
        regToken.setRegistrationToken("reg");
        when(verificationServerClient.getRegistrationToken(any())).thenReturn(regToken);

        when(dccServerClient.searchPublicKeys(anyString())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.createCertificate(quickTest);
        });
    }

    @Test
    void noQuicktestArchive() throws NoSuchAlgorithmException, IOException {
        QuickTest quickTest = createQuickTest();
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid(hashedGuid);

        RegistrationToken regToken = new RegistrationToken();
        regToken.setRegistrationToken("reg");
        when(verificationServerClient.getRegistrationToken(any())).thenReturn(regToken);

        DccPublicKey dccPublicKey = createDccPublicKey(sha256);
        when(dccServerClient.searchPublicKeys(anyString())).thenReturn(List.of(dccPublicKey));

        DccUploadResult dccUploadResult = new DccUploadResult();
        dccUploadResult.setPartialDcc("partial");
        when(dccServerClient.uploadDcc(anyString(), any())).thenReturn(dccUploadResult);

        DgcData dgcData = createDgcData();
        when(dgcCryptedPublisher.createDgc(any(), anyString(), any())).thenReturn(dgcData);

        when(dgcGenerator.dgcSetCosePartial(any(), any())).thenReturn("signed".getBytes());
        when(quickTestArchiveRepository.findByHashedGuid(anyString())).thenReturn(Optional.empty());

        underTest.createCertificate(quickTest);

        verify(dgcGenerator, never()).coseToQrCode(any());
        verify(pdfGenerator, never()).appendCertificatePage(any(), any(), anyString());
        verify(quickTestArchiveRepository, never()).saveAndFlush(any());
    }

    private DccPublicKey createDccPublicKey(String sha256) throws NoSuchAlgorithmException {
        DccPublicKey dccPublicKey = new DccPublicKey();
        dccPublicKey.setTestId(sha256);
        dccPublicKey.setPublicKey(genKeys());
        dccPublicKey.setDcci("dcci");
        return dccPublicKey;
    }

    private DgcData createDgcData() {
        DgcData dgcData = new DgcData();
        dgcData.setHash("hash".getBytes());
        dgcData.setDataEncrypted("encr".getBytes());
        dgcData.setDek("dek".getBytes());
        dgcData.setDccData("dcc".getBytes());
        return dgcData;
    }

    private QuickTest createQuickTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setHashedGuid(hashedGuid);
        quickTest.setTestResultServerHash(hashedGuid);
        quickTest.setCreatedAt(LocalDateTime.now());
        quickTest.setUpdatedAt(LocalDateTime.now());
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStandardisedGivenName("JOE");
        quickTest.setStandardisedFamilyName("MILLER");
        quickTest.setTestResult((short) 6);
        return quickTest;
    }

    private String genKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }


}