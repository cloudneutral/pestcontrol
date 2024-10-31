package io.cockroachdb.pestcontrol.schema;

import java.nio.file.Path;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Settings for connecting to a CockroachDB Cloud or Local cluster.
 */
public class ClusterProperties {
    private String clusterId;

    private ClusterType clusterType;

    private String adminUrl;

    private String apiKey;

    private Path certificatePath;

    private DataSourceProperties dataSourceProperties;

    public String getUsername() {
        return dataSourceProperties.getUsername();
    }

    public String getPassword() {
        return dataSourceProperties.getPassword();
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public Path getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(Path certificatePath) {
        this.certificatePath = certificatePath;
    }

    public DataSourceProperties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
