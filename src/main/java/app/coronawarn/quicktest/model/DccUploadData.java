package app.coronawarn.quicktest.model;

import lombok.Data;

@Data
public class DccUploadData {
    private String dccHash;
    private String dccEnrypted;
    private String dataEncryptionKey;
}
