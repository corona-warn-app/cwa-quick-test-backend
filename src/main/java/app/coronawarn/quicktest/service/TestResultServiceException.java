package app.coronawarn.quicktest.service;

import lombok.Getter;

@Getter
public class TestResultServiceException extends Exception {

    private final Reason reason;

    public TestResultServiceException(Reason reason) {
        super();
        this.reason = reason;
    }

    public enum Reason {
        SERVER_ERROR
    }

}
