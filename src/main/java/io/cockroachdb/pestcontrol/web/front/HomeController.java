package io.cockroachdb.pestcontrol.web.front;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pestcontrol.service.ClusterManager;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping
    public Callable<String> indexPage(Model model) {
        WebUtils.getAuthenticatedClusterProperties().ifPresent(properties -> {
            model.addAttribute("clusterProperties", properties);

            try {
                model.addAttribute("clusterVersion",
                        clusterManager.getClusterVersion(properties.getClusterId()));
            } catch (Exception e) {
                model.addAttribute("clusterVersion", "Unable to get version");
            }
        });
        return () -> "home";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }

    @GetMapping("/rels/{name}")
    public Callable<String> relPage(@PathVariable("name") String name, Model model) throws IOException {
        List<Extension> extensions = List.of(TablesExtension.create());

        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        Node document = parser.parseReader(
                new InputStreamReader(
                        new ClassPathResource("/templates/rels/%s.md".formatted(name))
                                .getInputStream()));
        String html = HtmlRenderer.builder()
                .extensions(extensions)
                .build()
                .render(document);
        // Hack
        html = html.replace("<table", "<table class='table'");

        model.addAttribute("html", html);

        return () -> "rel";
    }
}
