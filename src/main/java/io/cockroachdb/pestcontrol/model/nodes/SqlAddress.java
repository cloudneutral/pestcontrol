package io.cockroachdb.pestcontrol.model.nodes;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SqlAddress {
    @JsonProperty("network_field")
    private String networkField;

    @JsonProperty("address_field")
    private String addressField;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("network_field")
    public String getNetworkField() {
        return networkField;
    }

    @JsonProperty("network_field")
    public void setNetworkField(String networkField) {
        this.networkField = networkField;
    }

    @JsonProperty("address_field")
    public String getAddressField() {
        return addressField;
    }

    @JsonProperty("address_field")
    public void setAddressField(String addressField) {
        this.addressField = addressField;
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
