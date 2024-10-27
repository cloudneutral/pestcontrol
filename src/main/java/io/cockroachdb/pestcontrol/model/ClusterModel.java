package io.cockroachdb.pestcontrol.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.model.nodes.Locality;
import io.cockroachdb.pestcontrol.web.rest.LinkRelations;

/**
 * Representation model for a CockroachDB cluster composed by the locality
 * tiers region, zone and node.
 */
@Relation(value = LinkRelations.CLUSTER_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterModel extends RepresentationModel<ClusterModel> {
    private final ClusterProperties clusterProperties;

    private boolean available;

    private Collection<NodeModel> nodes = List.of();

    private ClusterModel(ClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public static ClusterModel available(ClusterProperties clusterProperties) {
        ClusterModel clusterModel = new ClusterModel(clusterProperties);
        clusterModel.setAvailable(true);
        return clusterModel;
    }

    public static ClusterModel unavailable(ClusterProperties clusterProperties) {
        ClusterModel clusterModel = new ClusterModel(clusterProperties);
        clusterModel.setAvailable(false);
        return clusterModel;
    }

    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    public String getId() {
        return clusterProperties.getClusterId();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Collection<NodeModel> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<NodeModel> nodes) {
        this.nodes = nodes;
    }

    public List<Locality> getLocalities() {
        List<Locality> localities = new ArrayList<>();
        nodes.forEach(node -> {
            Locality locality = node.getLocality();
            if (!localities.contains(locality)) {
                localities.add(locality);
            }
        });
        localities.sort(Comparator.comparing(Locality::toString));
        return localities;
    }

    /**
     * Find locality tiers up to a given sub-level.
     *
     * @param level the sub-level (1-based)
     * @return list of tiers
     */
    public List<Locality> getLocalities(int level) {
        List<Locality> tiers = new LinkedList<>();

        nodes.forEach(nodeModel -> {
            Locality subLocality = new Locality(new ArrayList<>(
                    nodeModel.getLocality().getTiers().stream().limit(level).toList()));
            if (!tiers.contains(subLocality)) {
                tiers.add(subLocality);
            }
        });

        return new ArrayList<>(tiers);
    }

    /**
     * Find nodes matching given locality tiers.
     *
     * @param locality the locality tiers
     * @return list of matching nodes, sorted by id in ascending order
     */
    public List<NodeModel> getNodes(Locality locality) {
        return nodes
                .stream()
                .filter(node -> node.getLocality().matches(locality.getTiers()))
                .sorted(Comparator.comparing(NodeModel::getId))
                .toList();
    }
}
