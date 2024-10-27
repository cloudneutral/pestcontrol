package io.cockroachdb.pestcontrol.service.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.domain.WorkerEntity;
import io.cockroachdb.pestcontrol.domain.WorkloadType;
import io.cockroachdb.pestcontrol.util.timeseries.Metrics;

@Component
public class WorkloadManager {
    private static final AtomicInteger monotonicIdGenerator = new AtomicInteger();

    /**
     * Map cluster id to active workload workers.
     */
    private final Map<String, ClusterWorkload> clusterWorkloads = new ConcurrentHashMap<>();

    @Autowired
    private WorkloadExecutor workloadExecutor;

    public void updateDataPoints() {
        clusterWorkloads.values()
                .parallelStream()
                .forEach(ClusterWorkload::updateDataPoints);
    }

    public void submitWorker(String clusterId,
                             Callable<?> action,
                             WorkloadType workloadType,
                             Duration duration) {
        final Instant stopTime = Instant.now().plus(duration);

        Metrics metrics = Metrics.empty();

        Future<?> future = workloadExecutor.submit(
                action,
                metrics,
                calls -> Instant.now().isBefore(stopTime));

        WorkerEntity workerEntity = new WorkerEntity(
                monotonicIdGenerator.incrementAndGet(),
                stopTime,
                workloadType.getDisplayValue(),
                future,
                metrics);

        clusterWorkloads.computeIfAbsent(clusterId, x -> new ClusterWorkload())
                .addWorker(workerEntity);
    }

    private ClusterWorkload clusterWorkload(String clusterId) {
        return clusterWorkloads.computeIfAbsent(clusterId, x -> new ClusterWorkload());
    }

    /**
     * Return time series sample interval (x-axis)
     */
    public List<Instant> getTimeSeriesInterval(String clusterId) {
        return clusterWorkload(clusterId)
                .getTimeSeriesInterval();
    }

    /**
     * Return time series sample values/metrics per workload (y-axis)
     *
     * @param id workload id
     */
    public List<Metrics> getTimeSeriesValues(String clusterId, Integer id) {
        return clusterWorkload(clusterId)
                .getTimeSeriesValues(id);
    }

    /**
     * Return aggregated time series for all workloads.
     *
     * @param clusterId static cluster id
     * @return aggregated time series metrics
     */
    public Metrics getAggregatedMetrics(String clusterId) {
        return clusterWorkload(clusterId)
                .getTimeSeriesAggregate();
    }

    public List<WorkerEntity> getWorkers(String clusterId) {
        return clusterWorkload(clusterId)
                .getWorkers();
    }

    public WorkerEntity findById(String clusterId, Integer id) {
        return clusterWorkload(clusterId)
                .findWorkerById(id);
    }

    public void deleteById(String clusterId, Integer id) {
        clusterWorkload(clusterId)
                .deleteWorker(id);
    }

    public void cancelAll(String clusterId) {
        clusterWorkload(clusterId).cancelWorkers();
    }

    public void deleteAll(String clusterId) {
        clusterWorkload(clusterId).clearWorkers();
    }

    public List<Map<String, Object>> getDataPoints(String clusterId, Function<Metrics, Double> mapper) {
        final List<Map<String, Object>> columnData = new ArrayList<>();

        {
            final Map<String, Object> headerElement = new HashMap<>();
            List<Long> labels =
                    getTimeSeriesInterval(clusterId)
                            .stream()
                            .map(Instant::toEpochMilli)
                            .toList();
            headerElement.put("data", labels.toArray());
            columnData.add(headerElement);
        }

        getWorkers(clusterId).forEach(workload -> {
            Map<String, Object> dataElement = new HashMap<>();

            List<Double> data =
                    getTimeSeriesValues(clusterId, workload.getId())
                            .stream()
                            .filter(metric -> !metric.isExpired())
                            .map(mapper)
                            .toList();

            dataElement.put("id", workload.getId());
            dataElement.put("name", "%s (%d)".formatted(workload.getTitle(), workload.getId()));
            dataElement.put("data", data.toArray());

            columnData.add(dataElement);
        });

        return columnData;
    }
}

