package io.cockroachdb.pestcontrol.service.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.util.Metrics;
import io.cockroachdb.pestcontrol.util.TimeUtils;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;
import jakarta.validation.constraints.NotNull;

@Relation(itemRelation = LinkRelations.WORKER_REL,
        collectionRelation = LinkRelations.WORKER_LIST_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkerModel extends RepresentationModel<WorkerModel> {
    private final String clusterId;

    private final Integer id;

    private final Instant startTime;

    private final Instant stopTime;

    private final String title;

    private final Metrics metrics;

    @JsonIgnore
    private final Future<?> future;

    public WorkerModel(String clusterId,
                       Integer id,
                       Instant startTime,
                       Instant stopTime,
                       String title,
                       Future<?> future,
                       Metrics metrics) {
        this.clusterId = clusterId;
        this.id = id;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.title = title;
        this.future = future;
        this.metrics = metrics;
    }

    public Integer getId() {
        return id;
    }

    public @NotNull String getClusterId() {
        return clusterId;
    }

    public Metrics getMetrics() {
        return isRunning() ? metrics : Metrics.copy(metrics);
    }

    public String getRemainingTime() {
        return TimeUtils.durationToDisplayString(getRemainingDuration());
    }

    @JsonIgnore
    public Duration getRemainingDuration() {
        return isRunning() ? Duration.between(Instant.now(), stopTime) : Duration.ofSeconds(0);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getStopTime() {
        return stopTime;
    }

    public String getTitle() {
        return title;
    }

    public boolean isRunning() {
        return !future.isDone();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean cancel() {
        return future.cancel(true);
    }

    public void awaitCompletion() {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkerModel workload = (WorkerModel) o;
        return Objects.equals(id, workload.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
