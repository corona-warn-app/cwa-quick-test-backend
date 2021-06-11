package eu.europa.ec.dgc;

import eu.europa.ec.dgc.dto.DgcData;
import eu.europa.ec.dgc.dto.DgcInitData;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DgcCryptedPublisherTest {
    private DgcCryptedPublisher dgcCryptedPublisher = new DgcCryptedPublisher();

    @Test
    void getEncodedDGCData() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String edgcJson = genSampleJson();
        String countryCode = "DE";
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiration = now.plus(Duration.of(365, ChronoUnit.DAYS));
        long issuedAt = now.toInstant().getEpochSecond();
        long expirationSec = expiration.toInstant().getEpochSecond();
        byte[] keyId = "dummy".getBytes(StandardCharsets.UTF_8);
        // We assume that it is EC Key
        int algId = 7;

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(3072);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        // Test coding of public key
        // Base64kodierte RSA3072 Public Key in x.509 Format (ohne PEM Header/Footer). Immer 564 Zeichen (als Base64Darstellung).
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        assertEquals(564,publicKeyBase64.length());

        DgcInitData dgcInitData = new DgcInitData();
        dgcInitData.setExpriation(expirationSec);
        dgcInitData.setIssuedAt(issuedAt);
        dgcInitData.setIssuerCode(countryCode);
        dgcInitData.setKeyId(keyId);
        dgcInitData.setAlgId(7);
        DgcData dgcData = dgcCryptedPublisher.createDgc(dgcInitData, edgcJson, keyPair.getPublic());

        // Base64kodierte und mit dem RSA Public Key verschlüsselter DEK. Der DEK selbst muss 32 Bytes haben (für AES256).
        // Der verschlüsselte DEK hat 384 Bytes und die base64kodierte Darstellung entsprechend 512 Zeichen.
        assertEquals(384,dgcData.getDek().length);
        String dekBase64 = Base64.getEncoder().encodeToString(dgcData.getDek());
        assertEquals(512,dekBase64.length());

        byte[] dccDecrypted = decryptDccData(dgcData.getDataEncrypted(), dgcData.getDek(), keyPair.getPrivate());
        assertArrayEquals(dgcData.getDccData(),dccDecrypted);
    }

    @Test
    void testExampleData() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // see CWA DCC Test Certificates v2.1.docx APPENDIX A
        byte[] clearText = Base64.getDecoder().decode("Yfj9EpJCA1mjnLdgCq38cRDpT/eClO8wecILFtC+jwZX5FGIvV8Cq4d0Hw" +
                "vTmR3hlflgo3oqWDG5nAECBXA5S3GrMpiLIL3VkMdSk9DqBA++Ue+25F5RMuNcE2SYmnHR0AoIqMFToPTwaeTlzgdnXK638RogN0" +
                "mHUrNN0bXfKJ5NBHYJ7R6MfPHL8iFZW/RnW3pthra20pjMSviVZJBu2ir1LGxnOpu86jL14PQSe8TdXwbglLop280p33MFiw7CDn" +
                "C0TQQJ59aphsLCqu9gbguD1uYQoSyNu3qeB3n7BNn0evHmhOi5Qjt1TXp6NtlJ4MlMeusiXGXGdlgCLBsVZaOm/QiSGbtcZ8uAH/" +
                "u+Me35CHLwBYoSLBU42diwrpRXGUGDwe0Oh1hKpy8Kd/i/YDj1/H1nQgwdZcjd7Ci5RRnFJ6QjZLqVT8A3SBfUtXP+ig2zRGDevJ" +
                "RXL34mBLM5NEYT9acaF6kST2LKEuEnVARBAEGCcqTWP39fm0TS2KnrYXX06+jLmkFT5sr1hLSBLmXtzrqA21mKpNwOYMMET4+W3/" +
                "5hjFBxZkVGCFwcXEaLNWGRTZBSog==");

        byte[] dek = Hex.decode("7427A22B0A0673B9F293935643DD4209ACEFA1CF944D2B9D9F97DD27474F12A7");

        byte[] encryptedDek = Base64.getDecoder().decode("+o5TT1onTB3M7mw2CxdAVTWYn961WWRyB4XvAtLotmbmUe1hkPY230" +
                "z90/L+q5BC6DQtIpyZ4oURDKwuC477JrCjws3j6J7g48q1QLxU96oJvjQAAo4rXb0liAVHW8x3Cv72gkbZS9cbzwZXsdb2VbNnHX" +
                "4tGt1TOPtDZMRUbwezwrOJv3K1ZH5lzkUEWcJVa5j5Ev/jkGhBf8b4stKrzxoyp/Ec4jRm48T+Dhiz2WjqUYU0Kh5/oR4mDhJS+" +
                "f8Unc/JBELGeKvBM+qVJEeHYiMvyl2aTkAwlPFYquattlgkKoVsAlWSrRs32XwagAog/Kjze+na0BJqqoYLysf9cpjqYmAA8bqK" +
                "1k58WT6aviNjMXjdrwjjdloMvChszjszpgFV78XpfL8SEW+JDdQg7lj5yO/rxyC8Q7MLX/45nsxpahoCvqLMoEj99P3ZU8ykKbFA" +
                "6Caac/ccHMVGTyYjMeyKUK2iFa4HercdT/8rjaEj99v81YDRkVICMPlNG");

        byte[] privateKey = Base64.getDecoder().decode("MIIG5QIBAAKCAYEAzrTf6abtRow4Z3zW/9glzOTZpbpanhU5QONkXSDPfi" +
                "dd9HpYtZUAkTYcMz0Z2tOAhS0O+mKjQdg48PxajR5FuheUpCBoJQOhcNJFwNt/FsYvxVLEg/WUE7oujOaRuk4gn6vzabU1pY7fj0C" +
                "468nNtkAl8eSKwGnzQWUAnhUWkpMb30Qe2Fm+kerEjQ8vymG671V2jXmfx0TmE347VYzrwuSonaSRdQkj1G2Kcu91HyoUGHwAD06x" +
                "qfH5T0o5m6fmQhdfy4t0jZef12sR5tISvRg1ojLnGatGTuVzbFZ6YrL5xV2Qxo8DL9NwN49GzwcQX1dYffEFEZjTMrn9A2/kCYT1q" +
                "F9Zh6YQ/4JUqu60m3bW2wqbVAzVB3fH4sMiqm+cUFftYnUS/TuDS7ZvaarN+SgBn60qPjAR3cG5vxx6VSs7tSI93t+8r1fyfpKu3L" +
                "ZGCKGQIM77/pNdvSKu0we5xbQSpvpPrKCrP96eJ5Ooqy1irCPpGv7FVUqsQjW1ghLxAgMBAAECggGAZb/bSKSGv+deDHMDUkUPtmA" +
                "gUtf8qzw/RpWoXb885Bh0w1dGO3vxH4nHL+GvzGu4I9YePVo4irzoqpdN65EvOX9KB9B781xqJ6MZ+ukREiDUewlW+q30ChHnwCvv" +
                "KAo1POPjmfE9SxWdLmLppAdeX4wMIZoiQrzkpRgWyboPtm77lVdu54ilqiYqQlVrbO/WmOE0zkdEQ07DAm1sv5vyoaj050SO5VFud" +
                "a7GK/V7ba/E3JiNXvuZsNIPqtwap2hpqSCxJkw1oL+0RD9HoO2ZKr6UxO3FjAYubiFIftvJyo/J3Zhu+dq4YKB9s5ZnOdATCewU2F" +
                "ahzBw4JIYlQVPnB9INQRdFX7GhtP36pxRIyd8eK5pJcEHBsUag5+40aH6aDqv039tvf+bARqQcbHGVkfWfhGaHmCMY9XrPiAM5DSG" +
                "kgiXjAqZxr5SEGmTXmKaXdEEQHer7Mw/YNFamTrSPH1n0+mR+NqetPQNRbWPHCzPP3iqlIbWHrvXeJ95VWv+RAoHBAN7Iveweqe1k" +
                "D2Aye8zWwNMQ24gAp2t+stvsSrbkOBfdhXxuKYQoEFNNBua9Vr/cv6Uc7djxCagAcA4VBMxeLuGvbLWPcXzmCgIYJf0p/u2FZ7AKI" +
                "UjclV7qbMn5b1yFUrlTcPtikYmdQ/g1ego4qPGWSKUk0A4bfjekUiSDqJBTTn8u1qXOj4N52KZ63XVnHxh1Nw44LBlnhokci+izI9" +
                "7k93be06H5yulQE45sHVhKZEADIIK6CPmtCjG+H6yYzwKBwQDthnr3B8HLO66nH5s1129N/JBCG5C0JM+5kemzGFw0+5K27134+B6" +
                "LMeU8ePlbozbXgFqsx+iZckNU5+5pNmYOdq5Y9DtHKcflxPexbVtCyFPxg2eU1B4dXEwH4FyQQ5bnTx1+x/r3M/kyZMdn57OOIWAl" +
                "8IenL+XjWOGhPa3t5Xer4qp66591ZYPnMdzJgHp+FonF6EkBvylZrd9TX76IPaErJNf5NeAUJd5L6piRfb5yRB47bvk0HcRdHtMeC" +
                "D8CgcEAzbgmkHPcTAaaoCTd5s/jrMMjRNn9vyh2ttfiQjJJjdgTnEwJcCyirpkxJz5tbavFGA23oB0r4oPM9WP5U1IhDmu0AY/cpw" +
                "2w7jAoXDc8XU5T6d3g0GxITiZAoN0BNTkzo0hmyutBIwcirX+MGwYpEzTIDh8JNA21JmJJK8ibjfIvSQgSU3eDVE/efBR2jSPVNft" +
                "/BFVge3D6bX/7vbw2AmwCCzVFzYthEMLN3DT/f/jpy6ZXjf+FrDBaeoZqrzFHAoHBAJdvEMCkQmrHPz+vx+3Yz07CQlrTZjtj6Mff" +
                "/kY/trHU3qIhFCGiVx7ZjdQzMI+7DEDyxVy5C1OhZMoiIH8VvpVkFx4BgDWLrrWQEXceSHvYaqRk6coaPqTrblHrOjiDzxbj+uMUr" +
                "q2ihjMZy4Q0Vea84qbtph7yS7fZt+hsAZLVpKmClrxNstlPAnyI7sHNVstCpU33HWyuqrdRQjvOpBQKbtGp6MQrFBTkW8i85c6Eg8" +
                "IoKboss1cFz2arO2A/UwKBwQCTvvM6sF8+3zw1EJRSecRzmAsxhJ8t0nkSM91ljITFeo8p5HdTeX/vYVgPAF2CCUQDW+kMgw4o5U+N" +
                "d24xm6zL2oYfjGe6lz/I+zM6hU9CPf1EdHxxccars7ilc/NrXAsaBJ7nO1p9peS1YqhoSC/x1aeJrcBDM1SlkoR0UG6l+hBVVLgs" +
                "96yJhG8WjgXJDjB24Nz+L8zuYIB3KUtMTtib4CxYZIvpXYSmzU8RCIa2g16i24dWhlos1QiWOlG28bI=");

        byte[] publicKey = Base64.getDecoder().decode("MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAzrTf6abtRow4Z3" +
                "zW/9glzOTZpbpanhU5QONkXSDPfidd9HpYtZUAkTYcMz0Z2tOAhS0O+mKjQdg48PxajR5FuheUpCBoJQOhcNJFwNt/FsYvxVLEg/W" +
                "UE7oujOaRuk4gn6vzabU1pY7fj0C468nNtkAl8eSKwGnzQWUAnhUWkpMb30Qe2Fm+kerEjQ8vymG671V2jXmfx0TmE347VYzrwuSo" +
                "naSRdQkj1G2Kcu91HyoUGHwAD06xqfH5T0o5m6fmQhdfy4t0jZef12sR5tISvRg1ojLnGatGTuVzbFZ6YrL5xV2Qxo8DL9NwN49Gz" +
                "wcQX1dYffEFEZjTMrn9A2/kCYT1qF9Zh6YQ/4JUqu60m3bW2wqbVAzVB3fH4sMiqm+cUFftYnUS/TuDS7ZvaarN+SgBn60qPjAR3c" +
                "G5vxx6VSs7tSI93t+8r1fyfpKu3LZGCKGQIM77/pNdvSKu0we5xbQSpvpPrKCrP96eJ5Ooqy1irCPpGv7FVUqsQjW1ghLxAgM" +
                "BAAE=");

        byte[] cipherText = Base64.getDecoder().decode(
                "y4N3agJtqoJrziH06swEHIoKi/CQDWF0Z0+/jdFuDif0h5VtQnjUcYzw33nnQE54mBjVyRH79ekdj/B3F9jJc/79f2V9DuPg" +
                        "/z0v0d/2Avpg85r8BiuDKDGskF7aZDVZO/wKWUZlj3vtT0aStFJCcgnZuQX83yTdJ22Ogmbn9uVcdv9xfT231aRnUr6y" +
                        "f4bON32LcWKxweXgvjp1gNZB/iJFURLKZfF/fZ66gGtcYh/bfT18re2DBNz0ZOpIUyOXuC4tNZA9x0A5hA4CXhaxunHX" +
                        "vFCgMCTsXl90JAG0cgfKD54oTp/EAnRcmMsZfAgfXqMLU4iEaWhj80A0nUltnrJx4JAvcrlSvBJa4716nDZaTzHTktzD" +
                        "NHl7r8J2wWh2SGFjSfi76s/mK54ZvM0x8B3+ppt/YxrJgAJ2e7SK0qz77dooMoL1G/qf6N2ZlIOSMsoSBrmYgGa5GAQb" +
                        "jMsLwoo7fKGYcR12ztp5J7it/w0xvx3jNbeNgxdLuco8gPoNLDxWElXGX1NS3umJ4yVkFKSPE5inUye3BO1pvWlXFmIM" +
                        "Fc0u+ZHurGbEMG/dHtVuK5ZKgp6tefd+L3VitTFeuQ==");

        assertEquals(439,clearText.length);
        assertEquals(384,encryptedDek.length);

        // Test data decoding

        Cipher cipher = Cipher.getInstance(DgcCryptedPublisher.DATA_CIPHER);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(dek,0,dek.length,"AES");
        SecretKey secretKey = secretKeyFactory.generateSecret(secretKeySpec);
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        assertArrayEquals(clearText, cipher.doFinal(cipherText));

        // Test dek decoding

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKeyInst = keyFactory.generatePublic(spec);

        PKCS8EncodedKeySpec  specPrivate = new PKCS8EncodedKeySpec(privateKey);
        PrivateKey privateKeyInst = keyFactory.generatePrivate(specPrivate);

        Cipher keyCipher = Cipher.getInstance(DgcCryptedPublisher.KEY_CIPHER);
        keyCipher.init(Cipher.DECRYPT_MODE, privateKeyInst);
        assertArrayEquals(dek,keyCipher.doFinal(encryptedDek));

    }

    @Test
    void testExampleData2() throws Exception {
        byte[] dek = Hex.decode("C392819D519DA6A615251E120D230F73D07DC96FC4FD881D781CC4089326AF29");
        byte[] privateKey = Base64.getDecoder().decode("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");
        byte[] encryptedDek = Base64.getDecoder().decode("fVz6ksmoW3DNExEl6HHXCnYVPKhH29OAvTF6UsmUaHcDnsj2XfQE3cnxSFU7rew9bmiGiIRWnxJv+ieckgcBww" +
                "uvZ51YCTRT3MILYHZ7jAfCltLes8o+yNFFL3sue4oHuEwTOFgFyZEnBcUiBK7Q9x12LYwAHqPsoKFK485cUzfTXkNesJaWQFC3qMW" +
                "H0A3xGxxfduVStzdkYXqaOw0ZL/qCZwG9ysi7+EqJ4HhecQ9/xuGxIyOpx5cOTOUN4FpMjt45yY1BT9xSCdn8akNIeEjIFuCS7C5S3x0kW71jzpLRxJCNA9b6iNFxdvIZYFMjVrvKIfs7q0M8K3fLfBqlksshzdRZ4vh0ysBHoqpJZnOhTvxo+v4xsFBA1n9NHX85aE+Vclk34WJ05T/tYQD/WAhvGvh6PrL5zTfkAxpinIvskviUNeOzkL0dKJyd2y6J+t//hp1ZKM6CUL8xW0vOTBT8mzNEbkPHorwPbbaO42fOjkQVkfaRWMt5kzH/mP0n");

        assertEquals(384,encryptedDek.length);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec  specPrivate = new PKCS8EncodedKeySpec(privateKey);
        PrivateKey privateKeyInst = keyFactory.generatePrivate(specPrivate);

        Cipher keyCipher = Cipher.getInstance(DgcCryptedPublisher.KEY_CIPHER);
        keyCipher.init(Cipher.DECRYPT_MODE, privateKeyInst);
        assertArrayEquals(dek,keyCipher.doFinal(encryptedDek));
    }

    private byte[] decryptDccData(byte[] encodedDccData, byte[] dek, PrivateKey privateKey)
            throws Exception {
        // decrypt RSA key
        Cipher keyCipher = Cipher.getInstance(DgcCryptedPublisher.KEY_CIPHER);
        keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] rsaKey = keyCipher.doFinal(dek);

        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(DgcCryptedPublisher.DATA_CIPHER);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(rsaKey,0,rsaKey.length,"AES");
        SecretKey secretKey = secretKeyFactory.generateSecret(secretKeySpec);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        return cipher.doFinal(encodedDccData);
    }

    public static String genSampleJson() {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        dccTestBuilder.gn("Artur").fn("Trzewik").gnt("ARTUR").fnt("TRZEWIK").dob("19730101");
        dccTestBuilder.detected(false)
                .dgci("URN:UVCI:01:OS:B5921A35D6A0D696421B3E2462178297I")
                .countryOfTest("DE")
                .testTypeRapid(true)
                .testingCentre("Hochdahl")
                .certificateIssuer("Dr Who")
                .sampleCollection(LocalDateTime.now());
        return dccTestBuilder.toJsonString();
    }
}