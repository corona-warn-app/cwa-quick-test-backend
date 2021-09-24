package app.coronawarn.quicktest.model.map;

import lombok.Data;

@Data
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
