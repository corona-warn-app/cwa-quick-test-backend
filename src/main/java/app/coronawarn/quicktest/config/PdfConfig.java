package app.coronawarn.quicktest.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("pdf")
public class PdfConfig {
    private String signatureText;
    private String positiveInstructionText;
    private String negativeInstructionText;
    private String testBrandNameDescriptionText;
    private String tradeNameEmptyText;
    private String testBrandIdDescriptionText;
    private String quickTestHeadlineText;
    private String authorPdfPropertiesText;
    private String creatorPdfPropertiesText;
    private String personPhoneDescriptionText;
    private String logoPath;
    private String quickTestOfDateText;
    private String personEmailDescriptionText;
    private String testResultDescriptionText;
    private String testResultPendingText;
    private String testResultNegativeText;
    private String testResultPositiveText;
    private String testResultDefaultText;
    private String executedByDescriptionText;
    private String furtherDataAboutThePersonText;
    private String genderDescriptionText;
    private String maleText;
    private String femaleText;
    private String diverseText;
    private String birthDateDescriptionText;
    private String furtherDataAboutTestDescriptionText;
    private String executedFromDescriptionText;
}
