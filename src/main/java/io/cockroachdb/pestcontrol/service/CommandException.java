package io.cockroachdb.pestcontrol.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CommandException extends ClusterException {
    private int errorCode = 0;

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
