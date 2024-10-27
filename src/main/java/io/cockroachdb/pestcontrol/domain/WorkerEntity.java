package io.cockroachdb.pestcontrol.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.util.TimeUtils;
import io.cockroachdb.pestcontrol.util.timeseries.Metrics;

//@Relation(itemRelation = LinkRelations.WORKLOAD_FORM_REL,
//        collectionRelation = LinkRelations.WORKLOAD_LIST_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkerEntity extends AbstractEntity<Integer> {
    private final Integer id;

    private final Instant stopTime;

    private final String title;

    @JsonIgnore
    private final Future<?> future;

    private final Metrics metrics;

    public WorkerEntity(Integer id,
                        Instant stopTime,
                        String title,
                        Future<?> future,
                        Metrics metrics) {
        this.id = id;
        this.stopTime = stopTime;
        this.title = title;
        this.future = future;
        this.metrics = metrics;
    }

    public Integer getId() {
        return id;
    }

    public Metrics getMetrics() {
        return isRunning() ? metrics : Metrics.expired(metrics);
    }

    public String getRemainingTime() {
        return TimeUtils.durationToDisplayString(
                isRunning() ? Duration.between(Instant.now(), stopTime) : Duration.ofSeconds(0));
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

    public boolean isFailed() {
        return false;
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
        WorkerEntity workload = (WorkerEntity) o;
        return Objects.equals(id, workload.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
