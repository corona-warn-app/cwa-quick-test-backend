package app.coronawarn.quicktest.model;

import lombok.Data;

@Data
public class DccPublicKey {
    private String testId;
    private String dcci;
    private String publicKey;
}
