package app.coronawarn.quicktest.domain;

import app.coronawarn.quicktest.dbencryption.DbEncryptionBooleanConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionByteArrayConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionSexTypeConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionShortConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionStringConverter;
import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class QuickTestArchive {

    static final long SERIAL_VERSION_UID = 1L;

    private String shortHashedGuid;

    @Id
    @Convert(converter = DbEncryptionStringConverter.class)
    private String hashedGuid;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean confirmationCwa;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String tenantId;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String pocId;

    @Convert(converter = DbEncryptionShortConverter.class)
    private Short testResult;

    @Setter(AccessLevel.NONE)
    @Version
    private Integer version;

    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean insuranceBillStatus;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String lastName;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String firstName;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String email;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String phoneNumber;

    @Convert(converter = DbEncryptionSexTypeConverter.class)
    private Sex sex;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String street;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String houseNumber;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String zipCode;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String city;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String testBrandId;

    @Convert(converter = DbEncryptionStringConverter.class)
    private String testBrandName;

    @Lob
    @Convert(converter = DbEncryptionByteArrayConverter.class)
    private byte[] pdf;
}
