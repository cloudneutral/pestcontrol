package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.model.ClusterModel;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.NodeModel;
import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.web.model.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterRestController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationModel applicationModel;

    @Autowired
    private ClusterModelAssembler clusterModelAssembler;

    @Autowired
    private NodeModelAssembler nodeModelAssembler;

    @GetMapping("/")
    public ResponseEntity<MessageModel> index() {
        MessageModel model = MessageModel.from("Cluster configuration index");
        model.add(linkTo(methodOn(ClusterRestController.class)
                .index())
                .withSelfRel());

        applicationModel.getClusterIds().forEach(clusterId -> {
            model.add(linkTo(methodOn(ClusterRestController.class)
                    .findCluster(clusterId))
                    .withRel(LinkRelations.CLUSTER_LIST_REL));
        });

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClusterModel> findCluster(@PathVariable("id") String id) {
        ClusterProperties clusterProperties = applicationModel.getClusterPropertiesById(id);

        ClusterModel clusterModel = ClusterModel.available(clusterProperties);
        clusterModel.setNodes(nodeModelAssembler.toCollectionModel(clusterManager.queryAllNodes(id)).getContent());

        return ResponseEntity.ok(clusterModelAssembler.toModel(clusterModel));
    }

    @GetMapping("/{id}/version")
    public ResponseEntity<MessageModel> getVersion(@PathVariable("id") String id) {
        MessageModel model = MessageModel
                .from(clusterManager.getClusterVersion(id));
        model.add(linkTo(methodOn(ClusterRestController.class)
                .getVersion(id))
                .withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{id}/region/{name}/disrupt")
    public ResponseEntity<NodeModel> disruptRegion(@PathVariable("id") String clusterId,
                                                   @PathVariable("name") String regionName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @PostMapping("/{id}/region/{name}/recover")
    public ResponseEntity<NodeModel> recoverRegion(@PathVariable("id") String clusterId,
                                                   @PathVariable("name") String regionName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
