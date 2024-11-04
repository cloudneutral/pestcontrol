package io.cockroachdb.pestcontrol.web.api.toxi;

import org.hibernate.validator.constraints.Range;
import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToxicForm extends RepresentationModel<ToxicForm> {
    private String proxy;

    @NotNull
    @Size(min = 1, message = "Name should contain at least 1 character")
    private String name;

    @NotNull
    private ToxicType toxicType;

    @NotNull
    private ToxicDirection toxicDirection;

    @NotNull
    @Range(min = 0, max = 100)
    private Integer toxicity;

    private Long latency;

    private Long jitter;

    private Long rate;

    private Long timeout;

    private Long bytes;

    private Long averageSize;

    private Long sizeVariation;

    private Long delay;

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull ToxicType getToxicType() {
        return toxicType;
    }

    public void setToxicType(@NotNull ToxicType toxicType) {
        this.toxicType = toxicType;
    }

    public @NotNull Integer getToxicity() {
        return toxicity;
    }

    public void setToxicity(@NotNull Integer toxicity) {
        this.toxicity = toxicity;
    }

    public @NotNull ToxicDirection getToxicDirection() {
        return toxicDirection;
    }

    public void setToxicDirection(
            @NotNull ToxicDirection toxicDirection) {
        this.toxicDirection = toxicDirection;
    }

    public Long getAverageSize() {
        return averageSize;
    }

    public void setAverageSize(Long averageSize) {
        this.averageSize = averageSize;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getLatency() {
        return latency;
    }

    public void setLatency(Long latency) {
        this.latency = latency;
    }

    public Long getJitter() {
        return jitter;
    }

    public void setJitter(Long jitter) {
        this.jitter = jitter;
    }

    public Long getRate() {
        return rate;
    }

    public void setRate(Long rate) {
        this.rate = rate;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getSizeVariation() {
        return sizeVariation;
    }

    public void setSizeVariation(Long sizeVariation) {
        this.sizeVariation = sizeVariation;
    }
}
