package app.coronawarn.quicktest.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_brand")
public class TestBrand {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "brand_name")
    private String brandname;

}
