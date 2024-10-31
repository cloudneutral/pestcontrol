package io.cockroachdb.pestcontrol.web.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.cockroachdb.pestcontrol.web.rest.LinkRelations;

@Relation(value = LinkRelations.TOXIC_REL,
        collectionRelation = LinkRelations.TOXIC_LIST_REL)
@JsonPropertyOrder({"links", "templates"})
public class ToxicModel extends RepresentationModel<ToxicModel> {
    private String name;

    private ToxicDirection stream;

    private float toxicity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ToxicDirection getStream() {
        return stream;
    }

    public void setStream(ToxicDirection stream) {
        this.stream = stream;
    }

    public float getToxicity() {
        return toxicity;
    }

    public void setToxicity(float toxicity) {
        this.toxicity = toxicity;
    }
}
