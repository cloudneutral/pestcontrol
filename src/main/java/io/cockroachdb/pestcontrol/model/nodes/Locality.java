package io.cockroachdb.pestcontrol.model.nodes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Locality {
    @JsonProperty("tiers")
    private List<Tier> tiers;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    public Locality() {
    }

    public Locality(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public boolean matches(List<Tier> required) {
        return required
                       .stream()
                       .filter(tier -> tiers.contains(tier))
                       .count() == required.size();
    }

    public Optional<String> findRegionTierValue() {
        return getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst()
                .map(Tier::getValue);
    }

    @JsonProperty("tiers")
    public List<Tier> getTiers() {
        return tiers;
    }

    @JsonProperty("tiers")
    public void setTiers(List<Tier> tiers) {
        this.tiers = tiers;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Locality locality = (Locality) o;
        return Objects.equals(toString(), locality.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(toString());
    }

    @Override
    public String toString() {
        List<String> tuples = new ArrayList<>();
        tiers.forEach(tier -> tuples.add(tier.getKey() + "=" + tier.getValue()));
        return String.join(",", tuples);
    }
}
