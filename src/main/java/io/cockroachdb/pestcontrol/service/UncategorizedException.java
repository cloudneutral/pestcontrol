package io.cockroachdb.pestcontrol.service;

public class UncategorizedException extends ClusterException {
    public UncategorizedException(String message) {
        super(message);
    }

    public UncategorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
