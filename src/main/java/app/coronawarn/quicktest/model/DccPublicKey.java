package app.coronawarn.quicktest.model;

import lombok.Data;

@Data
public class DccPublicKey {
    // !Warning. It is sha256 hash from send testId (or QuickTest.testResultServerHash)
    private String testId;
    private String dcci;
    private String publicKey;
}
