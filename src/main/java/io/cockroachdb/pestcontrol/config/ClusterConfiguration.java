package io.cockroachdb.pestcontrol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.service.CredentialsHandler;
import jakarta.annotation.PostConstruct;

@Configuration
public class ClusterConfiguration {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostConstruct
    public void init() {
        clusterManager.setCredentialsHandler(new CredentialsHandler() {
            @Override
            public Pair<String, String> getAuthenticationCredentials(String clusterId) {
                String username = applicationProperties.getClusterPropertiesById(clusterId).getDataSourceProperties()
                        .getUsername();
                String password = applicationProperties.getClusterPropertiesById(clusterId).getDataSourceProperties()
                        .getPassword();
                return Pair.of(username, password);
            }
        });
    }
}
