package io.cockroachdb.pestcontrol.web.model;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyForm extends RepresentationModel<ProxyForm> {
    @NotNull
    private String name;

    @NotNull
    private String listen;

    @NotNull
    private String upstream;

    public @NotNull String getListen() {
        return listen;
    }

    public void setListen(@NotNull String listen) {
        this.listen = listen;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getUpstream() {
        return upstream;
    }

    public void setUpstream(@NotNull String upstream) {
        this.upstream = upstream;
    }
}
