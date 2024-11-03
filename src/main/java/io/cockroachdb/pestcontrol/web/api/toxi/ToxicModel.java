package io.cockroachdb.pestcontrol.web.api.toxi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;

@Relation(value = LinkRelations.TOXIC_REL,
        collectionRelation = LinkRelations.TOXIC_LIST_REL)
@JsonPropertyOrder({"links", "templates"})
public class ToxicModel extends RepresentationModel<ToxicModel> {
    private String name;

    private ToxicType type;

    private ToxicDirection stream;

    private float toxicity;

    private Map<String, Object> attributes = new HashMap<>();

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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public ToxicModel addAttribute(String key, Object value) {
        this.attributes.put(key,value);
        return this;
    }

    public ToxicType getType() {
        return type;
    }

    public void setType(ToxicType type) {
        this.type = type;
    }
}
