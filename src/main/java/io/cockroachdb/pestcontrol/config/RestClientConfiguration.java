package io.cockroachdb.pestcontrol.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;

import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.service.ClientErrorException;
import io.cockroachdb.pestcontrol.service.ServerErrorException;

@Configuration
public class RestClientConfiguration {
    @Bean
    public RestClientProvider restClientProvider(SslBundles sslBundles, RestClientSsl ssl) {
        return clusterProperties -> EnumSet.of(ClusterType.local_secure, ClusterType.remote_secure)
                .contains(clusterProperties.getClusterType())
                ? sslRestClient(sslBundles, ssl) : defaultRestClient();
    }

    @Bean
    public RestClient sslRestClient(SslBundles sslBundles, RestClientSsl ssl) {
        final SslBundle sslBundle = sslBundles.getBundle("pestcontrol");

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories
                .get(ClientHttpRequestFactorySettings.DEFAULTS
                        .withReadTimeout(Duration.ofSeconds(10))
                        .withConnectTimeout(Duration.ofSeconds(10))
                        .withSslBundle(sslBundle)
                );

        return RestClient
                .builder()
                .apply(ssl.fromBundle(sslBundle))
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ClientErrorException(
                            "Request failed due to client error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ServerErrorException(
                            "Request failed due to CockroachDB server error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .build();
    }

    @Bean
    public RestClient defaultRestClient() {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories
                .get(ClientHttpRequestFactorySettings.DEFAULTS
                        .withReadTimeout(Duration.ofSeconds(10))
                        .withConnectTimeout(Duration.ofSeconds(10))
                );

        return RestClient
                .builder()
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ClientErrorException(
                            "Request failed due to client error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ServerErrorException(
                            "Request failed due to CockroachDB server error with status " + response.getStatusCode()
                            + ": " + body);
                })
                .build();
    }
}
