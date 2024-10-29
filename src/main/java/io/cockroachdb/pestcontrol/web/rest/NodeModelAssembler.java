package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.model.NodeModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class NodeModelAssembler extends RepresentationModelAssemblerSupport<NodeModel, NodeModel> {
    public NodeModelAssembler() {
        super(NodeRestController.class, NodeModel.class);
    }

    @Override
    public NodeModel toModel(NodeModel resource) {
        resource.add(linkTo(methodOn(NodeRestController.class)
                .getNode(resource.getClusterId(), resource.getId()))
                .withSelfRel());

//        resource.add(linkTo(methodOn(NodeRestController.class)
//                .getNodeDetail(resource.getClusterId(), resource.getId()))
//                .withRel(LinkRelations.NODE_DETAIL_REL)
//                .withTitle("Node details and statistics"));
//
//        resource.add(linkTo(methodOn(NodeRestController.class)
//                .getNodeStatus(resource.getClusterId(), resource.getId()))
//                .withRel(LinkRelations.NODE_STATUS_REL)
//                .withTitle("Node status and liveness metrics"));

        if ("true".equals(resource.getNodeStatus().getIsLive())) {
            if ("true".equals(resource.getNodeStatus().getIsAvailable())) {
                resource.add(linkTo(methodOn(NodeRestController.class)
                        .disruptNode(resource.getClusterId(), resource.getId()))
                        .withRel(LinkRelations.DISRUPT_REL)
                        .withTitle("Cause node disruption"));
            } else {
                resource.add(linkTo(methodOn(NodeRestController.class)
                        .recoverNode(resource.getClusterId(), resource.getId()))
                        .withRel(LinkRelations.RECOVER_REL)
                        .withTitle("Recover from node disruption"));
            }
        }

        return resource;
    }
}
