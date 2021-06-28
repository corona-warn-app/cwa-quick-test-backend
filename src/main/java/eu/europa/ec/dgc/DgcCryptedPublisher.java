/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc;

import com.upokecenter.cbor.CBORObject;
import eu.europa.ec.dgc.dto.DgcData;
import eu.europa.ec.dgc.dto.DgcInitData;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class DgcCryptedPublisher {
    public static final String KEY_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String DATA_CIPHER = "AES/CBC/PKCS5Padding";
    

    private final DgcGenerator dgcGenerator = new DgcGenerator();

    /**
     * create dgc data.
     * @param dgcInitData init params
     * @param dgcPayloadJson dcc payload
     * @param publicKey pubic key
     * @return data
     */
    public DgcData createDgc(DgcInitData dgcInitData, String dgcPayloadJson, PublicKey publicKey) {
        byte[] edgcCbor = dgcGenerator.genDgcCbor(dgcPayloadJson, dgcInitData.getIssuerCode(),
                dgcInitData.getIssuedAt(), dgcInitData.getExpriation());
        byte[] edgcCoseUnsigned = dgcGenerator.genCoseUnsigned(edgcCbor,dgcInitData.getKeyId(),dgcInitData.getAlgId());
        byte[] edgcHash = dgcGenerator.computeCoseSignHash(edgcCoseUnsigned);

        DgcData dgcData = new DgcData();
        dgcData.setHash(edgcHash);
        dgcData.setDccData(edgcCoseUnsigned);

        try {
            encryptData(dgcData, edgcCbor, publicKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }

        return dgcData;
    }

    private void encryptData(DgcData dgcData, byte[] cwtDccData, PublicKey publicKey) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();

        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(DATA_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        byte[] edgcDataEncrpyted = cipher.doFinal(cwtDccData);

        dgcData.setDataEncrypted(edgcDataEncrpyted);

        // encrypt RSA key
        Cipher keyCipher = Cipher.getInstance(KEY_CIPHER);
        OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec(
                "SHA-256","MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        );
        keyCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec);
        byte[] secretKeyBytes = secretKey.getEncoded();
        dgcData.setDek(keyCipher.doFinal(secretKeyBytes));

    }
}
