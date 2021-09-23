package app.coronawarn.quicktest.model.map;

import java.util.List;
import lombok.Data;

@Data
public class MapCenterList {
    Boolean deleteAll;
    List<MapEntryUploadData> centers;
}
