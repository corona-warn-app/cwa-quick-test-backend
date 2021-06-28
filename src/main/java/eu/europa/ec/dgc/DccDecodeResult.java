package eu.europa.ec.dgc;

import com.fasterxml.jackson.databind.JsonNode;

public class DccDecodeResult {
    private String issuer;
    private long issuedAt;
    private long expiration;
    private String ci;
    private String dccJsonString;
    private JsonNode dccJsonNode;

    public void setCi(String ci) {
        this.ci = ci;
    }

    public void setDccJsonString(String dccJsonString) {
        this.dccJsonString = dccJsonString;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public long getExpiration() {
        return expiration;
    }

    public String getCi() {
        return ci;
    }

    public String getDccJsonString() {
        return dccJsonString;
    }

    public void setDccJsonNode(JsonNode dccElem) {
        this.dccJsonNode = dccElem;
    }

    public JsonNode getDccJsonNode() {
        return dccJsonNode;
    }
}
