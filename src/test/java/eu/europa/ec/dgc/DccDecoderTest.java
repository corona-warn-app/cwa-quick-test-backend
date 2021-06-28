package eu.europa.ec.dgc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DccDecoderTest {
    @Test
    void decodeDcc() throws Exception {
        String dccQR = "HC1:NCFOXN%TSMAHN-HPTKCATYP7ABP:1EKW8VWBZEJ8USXG4H MYVQ/+6HYGH1VK1JZZPQA36S4VT6B69X5QB36FY1" +
                "OSMNV1L8VNF6H*MH:1TJE%+M1H61R6V EHPEB8ELTMAL61H63H6SW6+96RF667NZNM%BVPK9I+00DJZEBC5TFXK32KI6KNYJ+9" +
                "41VCSWC%PDX1LCTCTJCQEDFKD  CZ1L4005B91JP9-31JPDB5M835B9-NT0 2$$0X4PCY0+-C1W4/GJI+C7*4M1LHYAVCB570%" +
                "H10W52XE5SI:TU+MMPZ5X*H V1%+CE-4RZ4E%5MK90M91C5:G96CQH+9C9QQ E%E5TW5B/9BL5O+1RMHKY1$*UF/9BL5*ZE4C1" +
                "RU1G%5TW5A 6YO67N6T9EWFRUIFE2TIOB/0R .R0P9QNJJ6KXWNR*PK7UMKV9I2/-9802GS3J-NSAEWRU9-FSC520BP19X7LB/N" +
                "3H6F47-ZM28VW5DZWED-G:1F9SGW6F";
        DccDecoder dccDecoder = new DccDecoder();
        DccDecodeResult dccDecoderResult = dccDecoder.decodeDcc(dccQR);
        assertEquals("DE",dccDecoderResult.getIssuer());
        assertNotNull(dccDecoderResult.getDccJsonString());
        assertEquals("URN:UVCI:01:OS:B5921A35D6A0D696421B3E2462178297I",dccDecoderResult.getCi());
    }
}