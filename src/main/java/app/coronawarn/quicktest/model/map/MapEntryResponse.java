package app.coronawarn.quicktest.model.map;

import lombok.Data;

@Data
public class MapEntryResponse {
    String uuid;
    String name;
    String website;
    Coordinates coordinates;
    String logo;
    String marker;
    String address;
    String[] openingHours;
    String addressNote;
    String appointment;
    String[] testKinds;
    Boolean dcc;
    String message;
    String userReference;
    String enterDate;
    String leaveDate;

    @Data
    public class Coordinates {
        Integer longitude;
        Integer latitude;
    }
}
