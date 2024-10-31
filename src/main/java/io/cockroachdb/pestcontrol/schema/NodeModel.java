package io.cockroachdb.pestcontrol.schema;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.schema.nodes.NodeDetail;
import io.cockroachdb.pestcontrol.schema.status.NodeStatus;
import io.cockroachdb.pestcontrol.web.rest.LinkRelations;

/**
 * Combination of node detail and status.
 */
@Relation(value = LinkRelations.NODE_REL,
        collectionRelation = LinkRelations.NODE_LIST_REL)
@JsonPropertyOrder({"links", "templates"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeModel extends RepresentationModel<NodeModel> {
    private final String clusterId;

    @JsonProperty("detail")
    private NodeDetail nodeDetail;

    @JsonProperty("status")
    private NodeStatus nodeStatus;

    public NodeModel(String clusterId, NodeDetail nodeDetail, NodeStatus nodeStatus) {
        Assert.notNull(nodeDetail, "nodeDetail is null");
        Assert.notNull(nodeStatus, "nodeStatus is null");
        this.clusterId = clusterId;
        this.nodeDetail = nodeDetail;
        this.nodeStatus = nodeStatus;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getDescription() {
        return "Node " + getId()
               + ", " + nodeDetail.getLocality()
               + ", " + nodeDetail.getSqlAddress().getAddressField();
    }

    public Integer getId() {
        return nodeDetail.getNodeId();
    }

    public Locality getLocality() {
        return nodeDetail.getLocality();
    }

    public NodeDetail getNodeDetail() {
        return nodeDetail;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }
}
