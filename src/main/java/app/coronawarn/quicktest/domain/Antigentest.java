package app.coronawarn.quicktest.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Antigentest {

    @Id
    @Size(max = 10)
    private String id;

    @Size(max = 255)
    private String manufacturerName;

    @Size(max = 255)
    private String tradeName;

}
