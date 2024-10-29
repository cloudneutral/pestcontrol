package io.cockroachdb.pestcontrol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.rekawek.toxiproxy.ToxiproxyClient;
import io.cockroachdb.pestcontrol.model.ApplicationModel;

@Configuration
public class ToxiproxyConfiguration {
    @Autowired
    private ApplicationModel applicationModel;

    @Bean
    public ToxiproxyClient toxiproxyClient() {
        return new ToxiproxyClient(
                applicationModel.getToxiproxy().getHost(),
                applicationModel.getToxiproxy().getPort());
    }
}
