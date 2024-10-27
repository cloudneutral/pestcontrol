package io.cockroachdb.pestcontrol.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pestcontrol.model.ClusterProperties;

@FunctionalInterface
public interface RestClientProvider {
    RestClient matches(ClusterProperties clusterProperties);
}
