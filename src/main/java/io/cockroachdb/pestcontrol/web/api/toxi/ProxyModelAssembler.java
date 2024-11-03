package io.cockroachdb.pestcontrol.web.api.toxi;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.rekawek.toxiproxy.Proxy;
import io.cockroachdb.pestcontrol.web.ToxiproxyController;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProxyModelAssembler implements RepresentationModelAssembler<Proxy, ProxyModel> {
    @NotNull
    @Override
    public ProxyModel toModel(Proxy entity) {
        ProxyModel model = new ProxyModel();
        model.setName(entity.getName());
        model.setEnabled(entity.isEnabled());
        model.setListen(entity.getListen());
        model.setUpstream(entity.getUpstream());

        try {
            model.setToxics(entity.toxics().getAll().size());
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception retrieving proxy toxics", e);
        }

        Link selfLink = linkTo(methodOn(ToxiproxyRestController.class)
                .findProxy(entity.getName()))
                .withSelfRel()
                .andAffordance(afford(methodOn(ToxiproxyRestController.class).deleteProxy(entity.getName())))
                .andAffordance(afford(methodOn(ToxiproxyRestController.class).disableProxy(entity.getName())))
                .andAffordance(afford(methodOn(ToxiproxyRestController.class).enableProxy(entity.getName())));

        model.add(linkTo(methodOn(ToxiproxyRestController.class)
                .deleteProxy(entity.getName()))
                .withRel(LinkRelations.DELETE_REL));

        model.add(linkTo(methodOn(ToxiproxyController.class)
                .deleteProxy(entity.getName()))
                .withRel(LinkRelations.DELETE_REL + "-redirect"));

        if (model.isEnabled()) {
            model.add(linkTo(methodOn(ToxiproxyRestController.class)
                    .disableProxy(entity.getName()))
                    .withRel(LinkRelations.DISABLE_REL));

            model.add(linkTo(methodOn(ToxiproxyController.class)
                    .disableProxy(entity.getName()))
                    .withRel(LinkRelations.DISABLE_REL + "-redirect"));
        } else {
            model.add(linkTo(methodOn(ToxiproxyRestController.class)
                    .enableProxy(entity.getName()))
                    .withRel(LinkRelations.ENABLE_REL));

            model.add(linkTo(methodOn(ToxiproxyController.class)
                    .enableProxy(entity.getName()))
                    .withRel(LinkRelations.ENABLE_REL + "-redirect"));
        }

        model.add(linkTo(methodOn(ToxiproxyRestController.class)
                .findProxyToxics(entity.getName()))
                .withRel(LinkRelations.TOXIC_LIST_REL));

        return model.add(selfLink);
    }
};
