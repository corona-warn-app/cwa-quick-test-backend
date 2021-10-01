package app.coronawarn.quicktest.model.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;

@Data
public class MapEntrySingleResponse {
    @JsonProperty("UUID")
    String uuid;
    @JsonProperty("Name")
    String name;
    @JsonProperty("Website")
    String website;
    @JsonProperty("Longitude")
    Integer longitude;
    @JsonProperty("Latitude")
    Integer latitude;
    @JsonProperty("Address")
    String address;
    @JsonProperty("OpeningHours")
    String[] openingHours;
    @JsonProperty("AddressNote")
    String addressNote;
    @JsonProperty("Appointment")
    String appointment;
    @JsonProperty("TestKinds")
    String[] testKinds;
    @JsonProperty("DCC")
    Boolean dcc;
    @JsonProperty("Message")
    String message;
    @JsonProperty("UserReference")
    String userReference;
    @JsonProperty("EnterDate")
    String enterDate;
    @JsonProperty("LeaveDate")
    String leaveDate;


}
