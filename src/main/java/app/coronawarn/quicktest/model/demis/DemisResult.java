package app.coronawarn.quicktest.model.demis;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DemisResult {

    DemisStatus demisStatus;
    String details;
}
