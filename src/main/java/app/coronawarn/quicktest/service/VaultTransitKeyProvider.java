package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.ArchiveProperties;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultTransitContext;
import org.springframework.vault.support.VaultTransitKey;


@Primary
@Profile({ "cloud" })
@Service
@RequiredArgsConstructor
@Slf4j
public class VaultTransitKeyProvider implements KeyProvider {

    private final ArchiveProperties properties;
    private final VaultTemplate vaultTemplate;

    @Override
    public PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        VaultTransitKey publicKey = vaultTemplate
                .opsForTransit(properties.getVaultTransit().getFolder())
                .getKey(properties.getVaultTransit().getDek());
        String key = Integer.toString(publicKey.getLatestVersion());
        Map<String,String> keyValues = (Map<String, String>) publicKey.getKeys().get(key);
        String publicKeyStr = keyValues.get("public_key");
        // Remove -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY-----
        publicKeyStr = publicKeyStr.replaceAll("-----[A-Z ]+-----","");
        // Remove new lines too
        publicKeyStr = publicKeyStr.replaceAll("\\R","");
        byte[] encodedPublicKey = Base64.decode(publicKeyStr);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedPublicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    @Override
    public byte[] getPepper() {
        return this.properties.getHash().getPepper().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decrypt(String encrypted, String context) {
        log.info("KEY: encrypted {} - context {}", encrypted, context);
        byte[] decrypt = this.vaultTemplate
                .opsForTransit(properties.getVaultTransit().getFolder())
                .decrypt(properties.getVaultTransit().getDek(), encrypted,
                        VaultTransitContext.fromContext(Base64.encode(context.getBytes(StandardCharsets.UTF_8))));
        log.info("CIPHER: {}", new String(decrypt));
        String decode = new String(Base64.decode(decrypt));
        log.info("DECODED: {}", decode);
        return decode;
    }

    @Override
    public String encrypt(String plain, String context) {
        return this.vaultTemplate
                .opsForTransit(properties.getVaultTransit().getFolder())
                .encrypt(properties.getVaultTransit().getDek(),
                        Base64.encode(plain.getBytes(StandardCharsets.UTF_8)),
                        VaultTransitContext.fromContext(Base64.encode(context.getBytes(StandardCharsets.UTF_8))));
    }
}
