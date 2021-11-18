package app.coronawarn.quicktest.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quick_test_demis_receipt")
public class DemisReceipt {

    @Id
    @Column(name = "hashed_guid")
    private String hashedGuid;

    @Lob
    @Column(name = "pdf_receipt")
    private byte[] pdfReceipt;
}
