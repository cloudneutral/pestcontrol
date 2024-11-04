package io.cockroachdb.pestcontrol.web.api.toxi;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;
import io.cockroachdb.pestcontrol.service.ResourceNotFoundException;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;
import io.cockroachdb.pestcontrol.web.api.cluster.ClientModel;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/toxi")
public class ToxiproxyRestController {
    @Autowired
    private ToxiproxyClient toxiproxyClient;

    @Autowired
    private ProxyModelAssembler proxyModelAssembler;

    private Proxy findProxyByName(String name) {
        try {
            return toxiproxyClient
                    .getProxies()
                    .stream()
                    .filter(x -> name.equals(x.getName())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No such proxy with name: " + name));
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception retrieving proxies", e);
        }
    }

    @GetMapping()
    public ResponseEntity<ClientModel> index() {
        try {
            ClientModel model = new ClientModel();
            model.setVersion(toxiproxyClient.version());
            model.add(linkTo(methodOn(ToxiproxyRestController.class)
                    .index())
                    .withSelfRel());
            model.add(linkTo(methodOn(ToxiproxyRestController.class)
                    .reset())
                    .withRel(LinkRelations.RESET_REL)
                    .withTitle("Reset proxies"));
            model.add(linkTo(methodOn(ToxiproxyRestController.class)
                    .findProxies())
                    .withRel(LinkRelations.PROXY_LIST_REL)
                    .withTitle("Collection of proxies"));
            return ResponseEntity.ok(model);
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception retrieving client details", e);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ClientModel> reset() {
        try {
            toxiproxyClient.reset();
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception in client reset", e);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/proxy")
    public ResponseEntity<CollectionModel<ProxyModel>> findProxies() {
        try {
            CollectionModel<ProxyModel> collectionModel = proxyModelAssembler
                    .toCollectionModel(toxiproxyClient.getProxies());

            Links newLinks = collectionModel.getLinks().merge(Links.MergeMode.REPLACE_BY_REL,
                    linkTo(methodOn(ToxiproxyRestController.class).findProxies())
                            .withSelfRel()
                            .andAffordance(afford(methodOn(ToxiproxyRestController.class).newProxy(null))));

            return ResponseEntity.ok(CollectionModel.of(collectionModel.getContent(), newLinks));
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception retrieving proxies - is toxiproxy-server running?", e);
        }
    }

    @GetMapping(value = "/proxy/form")
    public HttpEntity<ProxyForm> getProxyForm() {
        ProxyForm form = new ProxyForm();
//        form.setName("crdb-1");
//        form.setListen("localhost:35258");
//        form.setUpstream("localhost:25258");

        return ResponseEntity.ok(form
                .add(linkTo(methodOn(ToxiproxyRestController.class)
                        .getProxyForm())
                        .withSelfRel()
                        .andAffordance(
                                afford(methodOn(ToxiproxyRestController.class)
                                        .newProxy(null)))
                ));
    }

    @PostMapping(value = "/proxy")
    public HttpEntity<ProxyModel> newProxy(@RequestBody @Valid ProxyForm form) {
        try {
            Proxy proxy = toxiproxyClient.createProxy(form.getName(), form.getListen(), form.getUpstream());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(proxyModelAssembler.toModel(proxy));
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception adding proxy", e);
        }
    }

    @GetMapping("/proxy/{name}")
    public ResponseEntity<ProxyModel> findProxy(
            @PathVariable("name") String name) {
        Proxy proxy = findProxyByName(name);
        return ResponseEntity.ok(proxyModelAssembler.toModel(proxy));
    }

    @PutMapping("/proxy/{name}/enable")
    public ResponseEntity<Void> enableProxy(
            @PathVariable("name") String name) {
        try {
            Proxy proxy = findProxyByName(name);
            proxy.enable();
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception enabling proxy", e);
        }
    }

    @PutMapping("/proxy/{name}/disable")
    public ResponseEntity<Void> disableProxy(
            @PathVariable("name") String name) {
        try {
            Proxy proxy = findProxyByName(name);
            proxy.disable();
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception disabling proxy", e);
        }
    }

    @DeleteMapping("/proxy/{name}/delete")
    public ResponseEntity<Void> deleteProxy(@PathVariable("name") String name) {
        try {
            Proxy proxy = findProxyByName(name);
            proxy.delete();
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception deleting proxy", e);
        }
    }

    @GetMapping("/proxy/{name}/toxic")
    public ResponseEntity<CollectionModel<ToxicModel>> findProxyToxics(
            @PathVariable("name") String name) {

        try {
            Proxy proxy = findProxyByName(name);

            CollectionModel<ToxicModel> collectionModel = new ToxicModelAssembler(name)
                    .toCollectionModel(proxy.toxics().getAll());

            Links newLinks = collectionModel.getLinks().merge(Links.MergeMode.REPLACE_BY_REL,
                    linkTo(methodOn(ToxiproxyRestController.class).findProxyToxics(name))
                            .withSelfRel()
                            .andAffordance(afford(methodOn(ToxiproxyRestController.class).newProxyToxic(name, null))));

            return ResponseEntity.ok(CollectionModel.of(collectionModel.getContent(), newLinks));
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception reading toxic", e);
        }
    }

    @GetMapping("/proxy/{name}/toxic/{toxic}")
    public ResponseEntity<ToxicModel> findProxyToxic(
            @PathVariable("name") String name,
            @PathVariable("toxic") String toxic) {
        try {
            Proxy proxy = findProxyByName(name);
            Toxic theToxic = proxy.toxics().get(toxic);

            ToxicModel model = new ToxicModelAssembler(name).toModel(theToxic);

            return ResponseEntity.ok(model);
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception reading toxic", e);
        }
    }

    @DeleteMapping("/proxy/{name}/toxic/{toxic}")
    public ResponseEntity<Void> deleteProxyToxic(
            @PathVariable("name") String name,
            @PathVariable("toxic") String toxic) {
        try {
            Proxy proxy = findProxyByName(name);
            Toxic t = proxy.toxics().get(toxic);
            t.remove();
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception removing toxic", e);
        }
    }

    @GetMapping(value = "/proxy/{name}/toxic/form")
    public HttpEntity<ToxicForm> getToxicForm(@PathVariable("name") String name) {
        ToxicForm form = new ToxicForm();
        form.setProxy(name);
        form.setToxicDirection(ToxicDirection.DOWNSTREAM);
        form.setToxicity(100);
        form.setLatency(150L);
        form.setJitter(15L);
        form.setAverageSize(150L);
        form.setSizeVariation(150L);
        form.setBytes(150L);
        form.setDelay(150L);
        form.setRate(16L);
        form.setTimeout(150L);

        return ResponseEntity.ok(form
                .add(linkTo(methodOn(ToxiproxyRestController.class)
                        .getToxicForm(name))
                        .withSelfRel()
                        .andAffordance(
                                afford(methodOn(ToxiproxyRestController.class)
                                        .newProxyToxic(name, null)))
                ));
    }

    @PostMapping(value = "/proxy/{name}/toxic")
    public HttpEntity<ToxicModel> newProxyToxic(
            @PathVariable("name") String name,
            @RequestBody @Valid ToxicForm form) {

        try {
            Proxy proxy = findProxyByName(name);

            ToxicType toxicType = form.getToxicType();

            Toxic toxic =
                    switch (toxicType) {
                        case LATENCY ->
                                proxy.toxics().latency(form.getName(), form.getToxicDirection(), form.getLatency());
                        case BANDWIDTH ->
                                proxy.toxics().bandwidth(form.getName(), form.getToxicDirection(), form.getRate());
                        case SLOW_CLOSE ->
                                proxy.toxics().slowClose(form.getName(), form.getToxicDirection(), form.getDelay());
                        case TIMEOUT ->
                                proxy.toxics().timeout(form.getName(), form.getToxicDirection(), form.getTimeout());
                        case SLICER -> proxy.toxics()
                                .slicer(form.getName(), form.getToxicDirection(), form.getAverageSize(),
                                        form.getDelay()).setSizeVariation(form.getSizeVariation());
                        case LIMIT_DATA ->
                                proxy.toxics().limitData(form.getName(), form.getToxicDirection(), form.getBytes());
                        case RESET_PEER ->
                                proxy.toxics().resetPeer(form.getName(), form.getToxicDirection(), form.getTimeout());
                    };

            toxic.setToxicity(form.getToxicity() / 100.0f);

            ToxicModel resource = new ToxicModelAssembler(name).toModel(toxic);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(resource);
        } catch (IOException e) {
            throw new ToxiproxyAccessException("I/O exception adding toxic", e);
        }
    }

}
