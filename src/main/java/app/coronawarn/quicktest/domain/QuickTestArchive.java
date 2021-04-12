package app.coronawarn.quicktest.domain;

import app.coronawarn.quicktest.dbencryption.DbEncryptionStringConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class QuickTestArchive extends QuickTest {

    @Lob
    @Convert(converter = DbEncryptionStringConverter.class)
    private String pdf;
}
