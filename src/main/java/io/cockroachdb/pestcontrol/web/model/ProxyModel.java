package io.cockroachdb.pestcontrol.web.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.web.rest.LinkRelations;

@Relation(value = LinkRelations.PROXY_REL,
        collectionRelation = LinkRelations.PROXY_LIST_REL)
@JsonPropertyOrder({"links", "templates"})
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyModel extends RepresentationModel<ProxyModel> {
    private String path;

    private String name;

    private String listen;

    private String upstream;

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getListen() {
        return listen;
    }

    public void setListen(String listen) {
        this.listen = listen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUpstream() {
        return upstream;
    }

    public void setUpstream(String upstream) {
        this.upstream = upstream;
    }
}
