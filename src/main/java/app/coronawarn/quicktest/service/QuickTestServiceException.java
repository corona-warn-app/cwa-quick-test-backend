package app.coronawarn.quicktest.service;

import lombok.Getter;

@Getter
public class QuickTestServiceException extends Exception {

    private final Reason reason;

    public QuickTestServiceException(Reason reason) {
        super();
        this.reason = reason;
    }

    public enum Reason {
        NOT_FOUND,
        INSERT_CONFLICT,
        UPDATE_NOT_FOUND,
        TEST_RESULT_SERVER_ERROR,
        INTERNAL_ERROR
    }
}
