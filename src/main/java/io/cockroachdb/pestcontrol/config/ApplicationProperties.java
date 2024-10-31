package io.cockroachdb.pestcontrol.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.schema.ClusterProperties;

@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    private List<ClusterProperties> clusters = new ArrayList<>();

    private Toxiproxy toxiproxy;

    public Toxiproxy getToxiproxy() {
        return toxiproxy;
    }

    public void setToxiproxy(Toxiproxy toxiproxy) {
        this.toxiproxy = toxiproxy;
    }

    public ClosableDataSource getDataSource(String clusterId) {
        return dataSourceFactory.apply(getClusterPropertiesById(clusterId).getDataSourceProperties());
    }

    public ClusterProperties getClusterPropertiesById(String clusterId) {
        return getClusters()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No cluster configuration with id: " + clusterId));
    }

    public List<String> getClusterIds() {
        return getClusters()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    public List<ClusterProperties> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterProperties> clusters) {
        this.clusters = clusters;
    }

}
