package io.cockroachdb.pestcontrol.web.model;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToxicForm extends RepresentationModel<ToxicForm> {
    @NotNull
    private String name;

    @NotNull
    private ToxicType toxicType;

    @NotNull
    private ToxicDirection toxicDirection;

    private Long latency;

    private Long rate;

    private Long timeout;

    private Long bytes;

    private Long averageSize;

    private Long sizeVariation;

    private Long delay;

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
