package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QuickTestArchiveDataView {

    private String hashedGuid;

    private String shortHashedGuid;

    private String tenantId;

    private String pocId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Persistence (Hibernate) version from 'quick_test_archive' table
    private Integer version;

    private Boolean confirmationCwa;

    private Short testResult;

    private Boolean privacyAgreement;

    private String lastName;

    private String firstName;

    private String email;

    private String phoneNumber;

    private Sex sex;

    private String street;

    private String houseNumber;

    private String zipCode;

    private String city;

    private String testBrandId;

    private String testBrandName;

    private String birthday;

    private String testResultServerHash;

    private String dcc;

    private String additionalInfo;

    private String groupName;
}
