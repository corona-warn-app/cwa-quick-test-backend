package app.coronawarn.quicktest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AntigenTest {

    private String testBrandId;
    private String testBrandName;
}
