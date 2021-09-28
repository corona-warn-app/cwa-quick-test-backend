package app.coronawarn.quicktest.model.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapEntryUploadData {
    String userReference;
    String name;
    String address;
    String[] testKinds;
    Boolean dcc;
    String website;
    String[] openingHours;
    String appointment;
}
