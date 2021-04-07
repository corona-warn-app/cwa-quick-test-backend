package app.coronawarn.quicktest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
