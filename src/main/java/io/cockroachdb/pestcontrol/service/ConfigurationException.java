package io.cockroachdb.pestcontrol.service;

public class ConfigurationException extends ClusterException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
