package app.coronawarn.quicktest.model.demis;

import java.util.EnumSet;

public enum DemisStatus {
    OK,
    DISABLED,
    INVALID_RESPONSE_BODY,
    SENDING_FAILED,
    ZIP_NOT_SUPPORTED,
    INVALID_INPUT,
    NONE;

    public static EnumSet<DemisStatus> getErrors() {
        return EnumSet.of(DISABLED, INVALID_INPUT, INVALID_RESPONSE_BODY, SENDING_FAILED, ZIP_NOT_SUPPORTED);
    }
}
