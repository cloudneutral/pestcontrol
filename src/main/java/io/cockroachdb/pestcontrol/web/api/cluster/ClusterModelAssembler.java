package io.cockroachdb.pestcontrol.web.api.cluster;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;
import io.cockroachdb.pestcontrol.web.api.workload.WorkloadRestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClusterModelAssembler implements RepresentationModelAssembler<ClusterModel, ClusterModel> {
    @Override
    public ClusterModel toModel(ClusterModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        resource.add(linkTo(methodOn(ClusterRestController.class)
                .getCluster(resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(ClusterRestController.class)
                .getVersion(resource.getId()))
                .withRel(LinkRelations.VERSION_REL)
                .withTitle("CockroachDB cluster version"));

        resource.add(linkTo(methodOn(LocalityRestController.class)
                .getLocalities(resource.getId()))
                .withRel(LinkRelations.LOCALITY_LIST_REL)
                .withTitle("Collection of locality tiers"));

        resource.add(linkTo(methodOn(NodeRestController.class)
                .getNodes(resource.getId()))
                .withRel(LinkRelations.NODE_LIST_REL)
                .withTitle("Collection of cluster nodes"));

        resource.add(linkTo(methodOn(WorkloadRestController.class)
                .getWorkers(resource.getId()))
                .withRel(LinkRelations.WORKER_LIST_REL)
                .withTitle("Collection of cluster workers"));

        resource.add(Link.of(resource.getClusterProperties().getAdminUrl())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("CockroachDB DB Console"));
        return resource;
    }
}
