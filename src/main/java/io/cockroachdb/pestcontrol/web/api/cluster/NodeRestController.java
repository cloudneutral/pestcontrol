package io.cockroachdb.pestcontrol.web.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.NodeDetail;
import io.cockroachdb.pestcontrol.schema.status.NodeStatus;
import io.cockroachdb.pestcontrol.service.ClusterManager;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class NodeRestController {
    @Autowired
    private NodeModelAssembler nodeModelAssembler;

    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/{clusterId}/node")
    public ResponseEntity<CollectionModel<NodeModel>> getNodes(
            @PathVariable("clusterId") String clusterId) {
        return ResponseEntity.ok(nodeModelAssembler.toCollectionModel(
                clusterManager.queryAllNodes(clusterId)));
    }

    @GetMapping("/{clusterId}/node/{id}")
    public ResponseEntity<NodeModel> getNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeModel nodeModel = clusterManager.queryNodeById(clusterId, id);
        return ResponseEntity.ok(nodeModelAssembler.toModel(nodeModel));
    }

    @GetMapping("/{clusterId}/node/{id}/detail")
    public ResponseEntity<EntityModel<NodeDetail>> getNodeDetail(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeDetail nodeDetail = clusterManager.queryNodeDetailById(clusterId, id);
        return ResponseEntity.ok(EntityModel.of(nodeDetail)
                .add(linkTo(methodOn(getClass())
                        .getNodeDetail(clusterId, id))
                        .withSelfRel()));
    }

    @GetMapping("/{clusterId}/node/{id}/status")
    public ResponseEntity<EntityModel<NodeStatus>> getNodeStatus(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeStatus nodeStatus = clusterManager.queryNodeStatusById(clusterId, id);
        return ResponseEntity.ok(EntityModel.of(nodeStatus)
                .add(linkTo(methodOn(getClass())
                        .getNodeStatus(clusterId, id))
                        .withSelfRel()));
    }

    @PostMapping("/{clusterId}/node/{id}/disrupt")
    public ResponseEntity<Void> disruptNode(@PathVariable("clusterId") String clusterId,
                                                 @PathVariable("id") Integer id) {
        clusterManager.disruptNode(clusterId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{id}/recover")
    public ResponseEntity<Void> recoverNode(@PathVariable("clusterId") String clusterId,
                                                 @PathVariable("id") Integer id) {
        clusterManager.recoverNode(clusterId, id);
        return ResponseEntity.ok().build();
    }
}
