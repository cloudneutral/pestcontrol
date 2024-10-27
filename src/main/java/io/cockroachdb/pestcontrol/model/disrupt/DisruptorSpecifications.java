package io.cockroachdb.pestcontrol.model.disrupt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "regional_disruptor_specifications"
})
public class DisruptorSpecifications {
    @JsonProperty("regional_disruptor_specifications")
    private List<RegionalDisruptorSpecification> regionalDisruptorSpecifications = new ArrayList<>();

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public void addRegionalDisruptorSpecification(
            RegionalDisruptorSpecification regionalDisruptorSpecification) {
        this.regionalDisruptorSpecifications.add(regionalDisruptorSpecification);
    }

    @JsonProperty("regional_disruptor_specifications")
    public List<RegionalDisruptorSpecification> getRegionalDisruptorSpecifications() {
        return regionalDisruptorSpecifications;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}

