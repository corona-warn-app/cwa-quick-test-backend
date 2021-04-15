package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.validation.ValidEmailSubject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(
        description = "Email model"
)
@Data
public class EmailMessage {

    List<@Email String> receivers;

    @NotNull
    @ValidEmailSubject
    String subject;

    @NotNull
    @Size(min = 1)
    String text;

}
