package io.cockroachdb.pestcontrol.web.api.workload;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.service.workload.WorkerModel;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class WorkerModelAssembler implements RepresentationModelAssembler<WorkerModel, WorkerModel> {
    @Override
    public WorkerModel toModel(WorkerModel resource) {
        if (resource.hasLinks()) {
            return resource;
        }

        Link selfLink = linkTo(methodOn(WorkloadRestController.class)
                .getWorker(resource.getClusterId(), resource.getId()))
                .withSelfRel();

        if (resource.isRunning()) {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadRestController.class)
                    .cancelWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.CANCEL_REL));
        } else {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId())));

            resource.add(linkTo(methodOn(WorkloadRestController.class)
                    .deleteWorker(resource.getClusterId(), resource.getId()))
                    .withRel(LinkRelations.DELETE_REL));
        }

        resource.add(selfLink);

        return resource;
    }
}
