package io.cockroachdb.pestcontrol.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.service.ClusterManager;

@Component
public class ApiAuthenticationService {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationModel applicationModel;

    public Authentication getAuthentication(String clusterId) {
//            throw new BadCredentialsException("Missing API key header: " + AUTH_TOKEN_HEADER_NAME);
        if (!clusterManager.hasSessionToken(clusterId)) {
            ClusterProperties clusterProperties
                    = applicationModel.getClusterPropertiesById(clusterId);
            clusterManager.login(clusterProperties.getClusterId(),
                    clusterProperties.getDataSourceProperties().getUsername(),
                    clusterProperties.getDataSourceProperties().getPassword()
            );
        }
        return new ApiAuthenticationToken(clusterId, AuthorityUtils.NO_AUTHORITIES);
    }
}
