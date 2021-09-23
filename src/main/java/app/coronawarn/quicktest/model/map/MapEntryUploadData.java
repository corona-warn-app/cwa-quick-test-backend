package app.coronawarn.quicktest.model.map;

import lombok.Data;

@Data
public class MapEntryUploadData {
    String userReference;
    String name;
    String website;
    String address;
    String[] testKinds;
    Boolean dcc;
}
