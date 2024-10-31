package io.cockroachdb.pestcontrol.service;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pestcontrol.schema.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.disrupt.DisruptorSpecifications;
import io.cockroachdb.pestcontrol.schema.disrupt.RegionalDisruptorSpecification;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.schema.nodes.Tier;

@Component
public class CloudDisruptionManager implements DisruptionManager {
    private static final String CLOUD_API_BASE = "https://cockroachlabs.cloud/api/v1";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.cloud_dedicated).contains(clusterType);
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel) {
        final Locality locality = nodeModel.getLocality();

        final String region = locality.getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("No locality or region key found: " + nodeModel.getLocality()))
                .getValue();

        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(false);
            regionalDisruptorSpecification.setRegionCode(region);
            regionalDisruptorSpecification.getPods().add("cockroachdb-" + nodeModel.getId());
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterProperties.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterProperties.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: " + responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel) {
        String bearerToken = clusterProperties.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterProperties.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: " + responseEntity.getBody());
    }

    @Override
    public void disruptLocality(ClusterProperties clusterProperties, Locality locality) {
        final RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        {
            regionalDisruptorSpecification.setIsWholeRegion(true);
            regionalDisruptorSpecification.setRegionCode(locality
                    .findRegionTierValue().orElseThrow( () -> new UnsupportedOperationException("No region tier found in locality: " + locality)));
        }

        final DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        final String bearerToken = clusterProperties.getApiKey();

        try {
            ResponseEntity<String> responseEntity = RestClient.create()
                    .put()
                    .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                            .formatted(clusterProperties.getClusterId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .body(disruptorSpecifications)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(String.class);
            logger.info("Disrupt command successful: " + responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Disrupt API command failed due to client error: "
                                           + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Disrupt API command failed due to server error: "
                                           + e.getMessage(), e);
        }
    }

    @Override
    public void recoverLocality(ClusterProperties clusterProperties, Locality locality) {
        String bearerToken = clusterProperties.getApiKey();

        ResponseEntity<String> responseEntity = RestClient.create()
                .put()
                .uri(CLOUD_API_BASE + "/clusters/%s/disrupt"
                        .formatted(clusterProperties.getClusterId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toEntity(String.class);

        logger.info("Disrupt recovery command successful: " + responseEntity.getBody());
    }
}
