package io.cockroachdb.pestcontrol.service.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import jakarta.servlet.http.HttpServletRequest;

public class ClusterAuthenticationDetails extends WebAuthenticationDetails {
    private final String clusterId;

    private final Boolean useFileCredentials;

    private ClusterProperties clusterProperties;

    public ClusterAuthenticationDetails(HttpServletRequest request) {
        super(request);

        this.clusterId = request.getParameter("clusterId");
        this.useFileCredentials = Boolean.valueOf(request.getParameter("useFileCredentials"));
    }

    public String getClusterId() {
        return clusterId;
    }

    public Boolean getUseFileCredentials() {
        return useFileCredentials;
    }

    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    public void setClusterProperties(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }
}
