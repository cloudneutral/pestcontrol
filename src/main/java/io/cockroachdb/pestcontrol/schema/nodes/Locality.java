package io.cockroachdb.pestcontrol.schema.nodes;

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

import io.cockroachdb.pestcontrol.util.PatternUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Locality {
    public static String toString(List<Tier> tiers) {
        List<String> tuples = new ArrayList<>();
        tiers.forEach(tier -> tuples.add(tier.getKey() + "=" + tier.getValue()));
        return String.join(",", tuples);
    }

    public static Locality fromTiers(String tiers) {
        List<Tier> tierList = new ArrayList<>();
        PatternUtils.parseLocality(tiers).forEach((k, v) -> {
            Tier tier = new Tier();
            tier.setKey(k);
            tier.setValue(v);
            tierList.add(tier);
        });
        return new Locality(tierList);
    }

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

    public String toTiers() {
        return toString();
    }

    @Override
    public String toString() {
        return Locality.toString(tiers);
    }
}
