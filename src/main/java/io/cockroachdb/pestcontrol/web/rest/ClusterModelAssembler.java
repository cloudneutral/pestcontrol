package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.schema.ClusterModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClusterModelAssembler implements RepresentationModelAssembler<ClusterModel, ClusterModel> {
    @Override
    public ClusterModel toModel(ClusterModel model) {
        model.add(linkTo(methodOn(ClusterRestController.class)
                .getCluster(model.getId()))
                .withSelfRel());
        model.add(linkTo(methodOn(ClusterRestController.class)
                .getVersion(model.getId()))
                .withRel(LinkRelations.VERSION_REL)
                .withTitle("CockroachDB cluster version"));

        model.add(linkTo(methodOn(LocalityRestController.class)
                .getLocalities(model.getId()))
                .withRel(LinkRelations.LOCALITY_LIST_REL)
                .withTitle("Collection of locality tiers"));

        model.add(linkTo(methodOn(NodeRestController.class)
                .getNodes(model.getId()))
                .withRel(LinkRelations.NODE_LIST_REL)
                .withTitle("Collection of cluster nodes"));

        model.add(linkTo(methodOn(WorkloadRestController.class)
                .getWorkers(model.getId()))
                .withRel(LinkRelations.WORKER_LIST_REL)
                .withTitle("Collection of cluster workers"));

        model.add(Link.of(model.getClusterProperties().getAdminUrl())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("CockroachDB DB Console"));
        return model;
    }
}
