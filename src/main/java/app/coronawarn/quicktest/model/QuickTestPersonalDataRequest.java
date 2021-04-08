package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.validation.ValidName;
import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Schema(
        description = "The quick test personaldata model."
)
@Data
public class QuickTestPersonalDataRequest {

    @NotNull
    private Boolean confirmationCwa;

    // TODO
    @NotNull
    private String testId;

    @NotNull
    private Boolean insBillStatus;

//    // TODO
//    @NotNull
//    private TestBrand testBrand;

    @ValidName
    @NotNull
    private String name;
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
    private Integer zipCode;

    // TODO
    @NotNull
    private String city;
}
