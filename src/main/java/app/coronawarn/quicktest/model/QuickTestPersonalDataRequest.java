package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.validation.ValidName;
import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(
        description = "The quick test personaldata model."
)
@Data
public class QuickTestPersonalDataRequest {

    @NotNull
    private Boolean confirmationCwa;

    @NotNull
    private Boolean insuranceBillStatus;

    @NotNull
    private String testBrandId;

    private String testBrandName;

    @ValidName
    @NotNull
    private String lastName;
    @ValidName
    @NotNull
    private String firstName;

    @Email
    @NotNull
    private String email;

    @ValidPhoneNumber
    @NotNull
    private String phoneNumber;

    @NotNull
    private Sex sex;

    // TODO
    @NotNull
    private String street;

    // TODO
    @NotNull
    private String houseNumber;

    // TODO
    @NotNull
    private String zipCode;

    // TODO
    @NotNull
    private String city;
}
