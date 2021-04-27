package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(
        description = "Response model for quick test archive entries."
)
@Data
public class QuickTestArchiveResponse {

    private String hashedGuid;

    private String lastName;

    private String firstName;

    private String email;

    private String phoneNumber;

    private Sex sex;

    private String street;

    private String houseNumber;

    private String zipCode;

    private String city;

    private String birthday;

    private String testResult;

}
