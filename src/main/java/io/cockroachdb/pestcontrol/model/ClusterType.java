package io.cockroachdb.pestcontrol.model;

public enum ClusterType {
    cloud_serverless,
    cloud_standard,
    cloud_dedicated,
    local_insecure,
    local_secure,
    remote_insecure,
    remote_secure,
}
