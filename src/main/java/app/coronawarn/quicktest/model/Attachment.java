package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(
        description = "Attachment file model"
)
@Data
public class Attachment {

    String name;
    byte[] data;

}
