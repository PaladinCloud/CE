package com.paladincloud.common.errors;

public class JobException extends RuntimeException {

    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
