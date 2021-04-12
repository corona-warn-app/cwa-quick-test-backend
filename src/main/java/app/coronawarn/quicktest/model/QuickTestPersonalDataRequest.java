package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.validation.ValidName;
import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import app.coronawarn.quicktest.validation.ValidZipCode;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @NotNull
    @Size(max = 200)
    private String street;

    @NotNull
    @Size(max = 10)
    private String houseNumber;

    @ValidZipCode
    @NotNull
    @Size(min = 5)
    @Size(max = 5)
    private String zipCode;

    @NotNull
    @Size(max = 255)
    private String city;
}
