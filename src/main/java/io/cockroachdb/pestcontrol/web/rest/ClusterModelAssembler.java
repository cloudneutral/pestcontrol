package io.cockroachdb.pestcontrol.web.rest;

import java.util.HashSet;
import java.util.Set;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.model.ClusterModel;
import io.cockroachdb.pestcontrol.model.ClusterType;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClusterModelAssembler extends RepresentationModelAssemblerSupport<ClusterModel, ClusterModel> {
    public ClusterModelAssembler() {
        super(ClusterRestController.class, ClusterModel.class);
    }

    @Override
    public ClusterModel toModel(ClusterModel resource) {
        resource.add(linkTo(methodOn(ClusterRestController.class)
                .getCluster(resource.getId()))
                .withSelfRel());
        resource.add(linkTo(methodOn(NodeRestController.class)
                .listNodes(resource.getId()))
                .withRel(LinkRelations.NODE_LIST_REL)
                .withTitle("Collection of nodes"));
        resource.add(linkTo(methodOn(ClusterRestController.class)
                .getVersion(resource.getId()))
                .withRel(LinkRelations.VERSION_REL)
                .withTitle("CockroachDB version"));
        resource.add(Link.of(resource.getClusterProperties().getAdminUrl())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("CockroachDB DB Console"));

        if (ClusterType.cloud_dedicated
                .equals(resource.getClusterProperties().getClusterType())) {
            final Set<String> regions = new HashSet<>();

            resource.getLocalities().forEach(locality ->
                    locality.findRegionTierValue()
                            .ifPresent(regions::add));

            regions.forEach(name -> {
                resource.add(linkTo(methodOn(ClusterRestController.class)
                        .disruptRegion(resource.getId(), name))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Cause region disruption"));

                resource.add(linkTo(methodOn(ClusterRestController.class)
                        .recoverRegion(resource.getId(), name))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Recover from region disruption"));
            });
        }
        return resource;
    }
}
