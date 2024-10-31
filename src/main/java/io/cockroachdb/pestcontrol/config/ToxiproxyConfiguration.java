package io.cockroachdb.pestcontrol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.rekawek.toxiproxy.ToxiproxyClient;

@Configuration
public class ToxiproxyConfiguration {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public ToxiproxyClient toxiproxyClient() {
        return new ToxiproxyClient(
                applicationProperties.getToxiproxy().getHost(),
                applicationProperties.getToxiproxy().getPort());
    }
}
