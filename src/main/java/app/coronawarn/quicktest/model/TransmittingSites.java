package app.coronawarn.quicktest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransmittingSites {

    @JsonProperty("TransmittingSite")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<TransmittingSite> transmittingSites = new ArrayList<>();

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransmittingSite {
        @JacksonXmlProperty(localName = "Name")
        private String name = "";

        @JacksonXmlProperty(localName = "Email")
        private String email = "";

        @JacksonXmlProperty(localName = "Covid19EMail")
        private String covid19EMail = "";

        @JsonProperty("SearchText")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<SearchText> searchTexts = new ArrayList<>();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchText {
        @JacksonXmlProperty(isAttribute = true, localName = "Value")
        private String value;
    }

}
