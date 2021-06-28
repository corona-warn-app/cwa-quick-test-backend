package eu.europa.ec.dgc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * Small utility to decode date from dcc.
 * It does not verify the CBOR signature and has only some structure checks.
 * It is opposite to {@link DgcGenerator}
 */
public class DccDecoder {
    /**
     * decode dcc.
     *
     * @param dccQrCode dcc qr code
     * @return DccDecodeResult
     */
    public DccDecodeResult decodeDcc(String dccQrCode) {
        if (!dccQrCode.startsWith("HC1:")) {
            throw new IllegalArgumentException("no HC1: prefix");
        }
        DccDecodeResult result = new DccDecodeResult();
        byte[] zippedCose = decode45(dccQrCode.substring(4));
        byte[] unzippedCose = unzip(zippedCose);
        CBORObject coseCbor = decodeTestCbor(unzippedCose);
        decodeTestCborPayload(coseCbor, result);
        return result;
    }

    private void decodeTestCborPayload(CBORObject coseCbor, DccDecodeResult result) {
        byte[] payloadBytes = coseCbor.get(2).GetByteString();
        CBORObject certData = CBORObject.DecodeFromBytes(payloadBytes);
        if (certData.getType() == CBORType.Map) {
            CBORObject cborIssuer = checkElemType(certData, 1, CBORType.TextString, "issuer");
            CBORObject cborIssuedAt = checkElemType(certData, 6, CBORType.Integer, "issued at");
            CBORObject cborExpiration = checkElemType(certData, 4, CBORType.Integer, "expiration");
            CBORObject hcert = checkElemType(certData, -260, CBORType.Map, "hcert");
            if (hcert != null) {
                CBORObject cborDcc = checkElemType(hcert, 1, CBORType.Map, "v1");
                result.setIssuer(cborIssuer.AsString());
                result.setIssuedAt(cborIssuedAt.AsInt64Value());
                result.setExpiration(cborExpiration.AsInt64Value());
                String jsonString = cborDcc.ToJSONString();
                result.setDccJsonString(jsonString);
                try {
                    extractCiFromJson(result);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("can not parse json", e);
                }
            }
        } else {
            throw new IllegalArgumentException("cose payload is not a Map");
        }
    }

    private void extractCiFromJson(DccDecodeResult result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dccElem = objectMapper.readTree(result.getDccJsonString());
        result.setDccJsonNode(dccElem);
        if (dccElem.isObject()) {
            if (!tryExtractCI(dccElem, "t", result)) {
                if (!tryExtractCI(dccElem, "r", result)) {
                    tryExtractCI(dccElem, "v", result);
                }
            }
        }
    }

    private boolean tryExtractCI(JsonNode dccElem, String key, DccDecodeResult result) {
        boolean success = false;
        JsonNode certArray = dccElem.get(key);
        if (certArray != null && certArray.isArray()) {
            for (JsonNode elem : certArray) {
                if (elem.isObject()) {
                    JsonNode ci = elem.get("ci");
                    if (ci != null && ci.isTextual()) {
                        result.setCi(ci.asText());
                        success = true;
                        break;
                    }
                }
            }
        }
        return success;
    }

    private CBORObject checkElemType(CBORObject certData, int key, CBORType valueType, String objectName) {
        CBORObject cborValue = certData.get(key);
        if (cborValue == null) {
            throw new IllegalArgumentException("missing payload key: " + key);
        } else {
            if (cborValue.getType() != valueType) {
                throw new IllegalArgumentException("wrong type of: " + objectName + " is: "
                        + cborValue.getType() + " expected: " + valueType);
            }
        }
        return cborValue;
    }

    private CBORObject decodeTestCbor(byte[] unzippedCose) {
        CBORObject cborObject = CBORObject.DecodeFromBytes(unzippedCose);
        if (cborObject.getType() != CBORType.Array
                || cborObject.size() < 4
                || !cborObject.isTagged()
                || 18 != cborObject.getMostInnerTag().ToInt32Checked()) {
            throw new IllegalArgumentException("unexpected cose structure");
        }
        if (cborObject.get(2).getType() != CBORType.ByteString) {
            throw new IllegalArgumentException("unexpected cose payload is not byte string");
        }
        return cborObject;
    }

    private byte[] unzip(byte[] zippedCose) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zippedCose);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        byte[] uncompressed;
        try {
            uncompressed = inflaterInputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return uncompressed;
    }

    private byte[] decode45(String dccQrCode) {
        return Base45Encoder.decodeFromString(dccQrCode);
    }

}
