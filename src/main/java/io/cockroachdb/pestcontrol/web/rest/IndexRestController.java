package io.cockroachdb.pestcontrol.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.web.model.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexRestController {
    @Autowired
    private ApplicationModel applicationModel;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = MessageModel.from("Pest Control Hypermedia API");
        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel()
                .withTitle("API index"));

        applicationModel.getClusterIds().forEach(clusterId -> {
            index.add(linkTo(methodOn(ClusterRestController.class)
                    .getCluster(clusterId))
                    .withRel(LinkRelations.CLUSTER_REL)
                    .withTitle("Cluster details"));
        });

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel(LinkRelations.ACTUATORS_REL)
                .withTitle("Spring boot actuators"));

        return ResponseEntity.ok(index);
    }
}
