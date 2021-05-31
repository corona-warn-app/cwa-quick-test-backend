package eu.europa.ec.dgc;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DccTestBuilder {
    private JsonNodeFactory jsonNodeFactory;
    private ObjectNode dccObject;
    private ObjectNode nameObject;
    private ObjectNode testObject;

    private DateTimeFormatter dateFormat;


    public DccTestBuilder() {
        dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        jsonNodeFactory = JsonNodeFactory.instance;
        dccObject = jsonNodeFactory.objectNode();
        nameObject = jsonNodeFactory.objectNode();
        testObject = jsonNodeFactory.objectNode();
        dccObject.set("version",jsonNodeFactory.textNode("1.0.0"));
        dccObject.set("nam",nameObject);
        ArrayNode testArray = jsonNodeFactory.arrayNode();
        testArray.add(testObject);
        // disease-agent-targeted COVID-19
        // see https://github.com/ehn-digital-green-development/ehn-dgc-schema/blob/main/valuesets/disease-agent-targeted.json
        testObject.set("tg",jsonNodeFactory.textNode("840539006"));
        dccObject.set("t",testArray);
    }

    public DccTestBuilder fn(String fn) {
        nameObject.set("fn",jsonNodeFactory.textNode(fn));
        return this;
    }

    public DccTestBuilder gn(String gn) {
        nameObject.set("gn",jsonNodeFactory.textNode(gn));
        return this;
    }

    public DccTestBuilder fnt(String fnt) {
        nameObject.set("fnt",jsonNodeFactory.textNode(fnt));
        return this;
    }

    public DccTestBuilder gnt(String gnt) {
        nameObject.set("fnt",jsonNodeFactory.textNode(gnt));
        return this;
    }

    public String toJsonString() {
        return dccObject.toString();
    }

    public DccTestBuilder dgci(String dgci) {
        testObject.set("ci",jsonNodeFactory.textNode(dgci));
        return this;
    }

    public DccTestBuilder dob(String birthday) {
        dccObject.set("dob",jsonNodeFactory.textNode(birthday));
        return this;
    }

    public DccTestBuilder detected(boolean covidDetected) {
        // https://github.com/ehn-digital-green-development/ehn-dgc-schema/blob/main/valuesets/test-result.json
        testObject.set("tr",jsonNodeFactory.textNode(covidDetected ? "260373001" : "260415000"));
        return this;
    }

    public DccTestBuilder testTypeRapid(boolean isRapidTest) {
        testObject.set("tt",jsonNodeFactory.textNode(isRapidTest ? "LP217198-3" : "LP6464-4"));
        return this;
    }

    public DccTestBuilder countryOfTest(String co) {
        testObject.set("co",jsonNodeFactory.textNode(co));
        return this;
    }

    public DccTestBuilder certificateIssuer(String is) {
        testObject.set("is",jsonNodeFactory.textNode(is));
        return this;
    }

    public DccTestBuilder testingCentre(String tc) {
        testObject.set("tc",jsonNodeFactory.textNode(tc));
        return this;
    }

    public DccTestBuilder sampleCollection(LocalDateTime dateTime) {
        testObject.set("sc",jsonNodeFactory.textNode(toISO8601(dateTime)));
        return this;
    }

    public String toISO8601(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneOffset.UTC).format(dateFormat);
    }

}
