package io.cockroachdb.pestcontrol.web.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

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
                .withTitle("Hypermedia API index"));

        index.add(linkTo(methodOn(ClusterRestController.class)
                .index())
                .withRel(LinkRelations.CLUSTER_REL)
                .withTitle("Cluster configuration index"));

        index.add(linkTo(methodOn(WorkloadRestController.class)
                .index())
                .withRel(LinkRelations.WORKLOAD_INDEX_REL)
                .withTitle("Cluster workload index"));

        index.add(linkTo(methodOn(ToxiproxyRestController.class)
                .index())
                .withRel(LinkRelations.TOXI_PROXY_CLIENT_REL)
                .withTitle("Toxiproxy client"));

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel(LinkRelations.ACTUATORS_REL)
                .withTitle("Spring boot actuators"));

        return ResponseEntity.ok(index);
    }

    @GetMapping("/browser")
    public RedirectView halExplorerPage() {
        String rootUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("api")
                .buildAndExpand()
                .toUriString();
        return new RedirectView("/browser/index.html#theme=Darkly&uri=" + rootUri);
    }

    @GetMapping("/rels/{name}")
    public ModelAndView relPage(
            @PathVariable("name") String name) throws IOException {
        ClassPathResource path = new ClassPathResource("/templates/rels/%s.md".formatted(name));
        if (!path.isReadable()) {
            path = new ClassPathResource("/templates/rels/default.md");
        }

        final List<Extension> extensions = List.of(TablesExtension.create());

        String html = HtmlRenderer.builder()
                .extensions(extensions)
                .build()
                .render(Parser.builder().extensions(extensions).build()
                        .parseReader(new InputStreamReader(path.getInputStream())));
        // Hack!
        html = html.replace("<table", "<table class='table'");
        return new ModelAndView("rel", "html", html);
    }
}
