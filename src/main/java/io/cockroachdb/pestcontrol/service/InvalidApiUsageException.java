package io.cockroachdb.pestcontrol.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidApiUsageException extends ClusterException {
    public InvalidApiUsageException(String message) {
        super(message);
    }

    public InvalidApiUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
