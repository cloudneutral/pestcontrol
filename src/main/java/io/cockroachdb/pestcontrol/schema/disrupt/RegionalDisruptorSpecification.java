package io.cockroachdb.pestcontrol.schema.disrupt;

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
        "region_code",
        "is_whole_region",
        "pods"
})
public class RegionalDisruptorSpecification {
    @JsonProperty("region_code")
    private String regionCode;

    @JsonProperty("is_whole_region")
    private Boolean isWholeRegion;

    @JsonProperty("pods")
    private List<String> pods = new ArrayList<>();

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("region_code")
    public String getRegionCode() {
        return regionCode;
    }

    @JsonProperty("region_code")
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    @JsonProperty("is_whole_region")
    public Boolean getIsWholeRegion() {
        return isWholeRegion;
    }

    @JsonProperty("is_whole_region")
    public void setIsWholeRegion(Boolean isWholeRegion) {
        this.isWholeRegion = isWholeRegion;
    }

    @JsonProperty("pods")
    public List<String> getPods() {
        return pods;
    }

    @JsonProperty("pods")
    public void setPods(List<String> pods) {
        this.pods = pods;
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