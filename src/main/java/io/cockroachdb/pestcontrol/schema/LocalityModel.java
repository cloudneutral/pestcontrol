package io.cockroachdb.pestcontrol.schema;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.schema.nodes.Tier;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;

@Relation(value = LinkRelations.LOCALITY_REL,
        collectionRelation = LinkRelations.LOCALITY_LIST_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class LocalityModel extends RepresentationModel<LocalityModel> {
    @JsonProperty("tiers")
    private final List<Tier> tiers;

    public LocalityModel(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    public String toTiers() {
        return Locality.toString(tiers);
    }
}
