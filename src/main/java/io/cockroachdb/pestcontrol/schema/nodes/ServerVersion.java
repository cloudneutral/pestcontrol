package io.cockroachdb.pestcontrol.schema.nodes;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerVersion {
    @JsonProperty("major")
    private Integer major;

    @JsonProperty("minor")
    private Integer minor;

    @JsonProperty("patch")
    private Integer patch;

    @JsonProperty("internal")
    private Integer internal;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("major")
    public Integer getMajor() {
        return major;
    }

    @JsonProperty("major")
    public void setMajor(Integer major) {
        this.major = major;
    }

    @JsonProperty("minor")
    public Integer getMinor() {
        return minor;
    }

    @JsonProperty("minor")
    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    @JsonProperty("patch")
    public Integer getPatch() {
        return patch;
    }

    @JsonProperty("patch")
    public void setPatch(Integer patch) {
        this.patch = patch;
    }

    @JsonProperty("internal")
    public Integer getInternal() {
        return internal;
    }

    @JsonProperty("internal")
    public void setInternal(Integer internal) {
        this.internal = internal;
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
