package io.cockroachdb.pestcontrol.schema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.schema.nodes.Tier;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;

/**
 * Representation model for a CockroachDB cluster composed by the locality
 * tiers region, zone and node.
 */
@Relation(value = LinkRelations.CLUSTER_REL,
        collectionRelation = LinkRelations.CLUSTER_LIST_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class ClusterModel extends RepresentationModel<ClusterModel> {
    public static ClusterModel from(ClusterProperties clusterProperties) {
        return new ClusterModel(clusterProperties);
    }

    @JsonProperty("properties")
    private final ClusterProperties clusterProperties;

    @JsonIgnore
    private CollectionModel<NodeModel> nodes = CollectionModel.empty();

    @JsonIgnore
    private CollectionModel<LocalityModel> localities = CollectionModel.empty();

    private ClusterModel(ClusterProperties clusterProperties) {
        Assert.notNull(clusterProperties, "clusterProperties is null");
        this.clusterProperties = clusterProperties;
    }

    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    public String getId() {
        return clusterProperties.getClusterId();
    }

    public CollectionModel<NodeModel> getNodes() {
        return nodes;
    }

    public void setNodes(CollectionModel<NodeModel> nodes) {
        this.nodes = nodes;
    }

    public void setLocalities(CollectionModel<LocalityModel> localities) {
        this.localities = localities;
    }

    public CollectionModel<LocalityModel> getLocalities() {
        return localities;
    }

    /**
     * Find locality tiers up to a given sublevel.
     *
     * @param level the sublevel (1-based)
     * @return list of tiers
     */
    public List<LocalityModel> getLocalities(int level) {
        // Keep insertion order
        Set<LocalityModel> subLocalities = new LinkedHashSet<>();
//                Comparator.comparing(LocalityModel::toTiers));

        localities.forEach(localityModel -> {
            LocalityModel subLocality = new LocalityModel(new ArrayList<>(localityModel
                    .getTiers()
                    .stream()
                    .limit(level)
                    .toList()))
                    .add(localityModel.getLinks());

            subLocalities.stream()
                    .filter(x -> x.toTiers().equals(subLocality.toTiers()))
                    .findFirst()
                    .ifPresentOrElse(localityModel1 -> {}, () -> subLocalities.add(subLocality));
        });

        return new ArrayList<>(subLocalities);
    }

    /**
     * Find nodes matching a given list of locality tiers.
     *
     * @param tiers the locality tiers
     * @return list of matching nodes, sorted by node ID in ascending order
     */
    public List<NodeModel> getNodes(List<Tier> tiers) {
        return nodes
                .getContent()
                .stream()
                .filter(node -> node.getLocality().matches(tiers))
                .sorted(Comparator.comparing(NodeModel::getId))
                .toList();
//                        .sorted((n1, n2) -> n1.getLocality().toTiers()
//                .compareToIgnoreCase(n2.getLocality().toTiers()))
    }
}
