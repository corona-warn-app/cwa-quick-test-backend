package eu.europa.ec.dgc;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

// TODO do add all field, test input data and all mandatory
public class DccTestBuilder {
    private JsonNodeFactory jsonNodeFactory;
    private ObjectNode dccObject;
    private ObjectNode nameObject;
    private ObjectNode testObject;

    public DccTestBuilder() {
        jsonNodeFactory = JsonNodeFactory.instance;
        dccObject = jsonNodeFactory.objectNode();
        nameObject = jsonNodeFactory.objectNode();
        testObject = jsonNodeFactory.objectNode();
        dccObject.set("version",jsonNodeFactory.textNode("1.0.0"));
        dccObject.set("nam",nameObject);
        ArrayNode testArray = jsonNodeFactory.arrayNode();
        testArray.add(testObject);
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
}
