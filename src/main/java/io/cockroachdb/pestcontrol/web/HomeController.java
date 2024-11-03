package io.cockroachdb.pestcontrol.web;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.web.push.MessageModel;
import io.cockroachdb.pestcontrol.web.api.FakeException;

@WebController
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

    @GetMapping("/fakeerror")
    public ResponseEntity<MessageModel> errorOnGet() {
        throw new FakeException("Fake exception!", new IOException("I/O disturbance!"));
    }

    @PutMapping("/fakeerror")
    public ResponseEntity<MessageModel> errorOnPut() {
        throw new FakeException("Fake exception!", new IOException("I/O disturbance!"));
    }

    @PostMapping("/fakeerror")
    public ResponseEntity<MessageModel> errorOnPost() {
        throw new FakeException("Fake exception!", new IOException("I/O disturbance!"));
    }

    @DeleteMapping("/fakeerror")
    public ResponseEntity<MessageModel> errorOnDelete() {
        throw new FakeException("Fake exception!", new IOException("I/O disturbance!"));
    }
}
