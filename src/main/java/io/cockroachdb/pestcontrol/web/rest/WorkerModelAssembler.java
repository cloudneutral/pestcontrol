package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.schema.WorkerModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class WorkerModelAssembler implements RepresentationModelAssembler<WorkerModel,WorkerModel> {
    @Override
    public WorkerModel toModel(WorkerModel model) {
        Link selfLink = linkTo(methodOn(WorkloadRestController.class)
                .getWorker(model.getClusterId(), model.getId()))
                .withSelfRel();

        if (model.isRunning()) {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .cancelWorker(model.getClusterId(), model.getId())));

            model.add(linkTo(methodOn(WorkloadRestController.class)
                    .cancelWorker(model.getClusterId(), model.getId()))
                    .withRel(LinkRelations.CANCEL_REL));
        } else {
            selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                    .deleteWorker(model.getClusterId(), model.getId())));

            model.add(linkTo(methodOn(WorkloadRestController.class)
                    .deleteWorker(model.getClusterId(), model.getId()))
                    .withRel(LinkRelations.DELETE_REL));
        }

        model.add(selfLink);

        return model;
    }
}
