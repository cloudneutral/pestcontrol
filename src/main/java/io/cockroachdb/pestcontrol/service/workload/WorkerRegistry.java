package io.cockroachdb.pestcontrol.service.workload;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.cockroachdb.pestcontrol.schema.WorkerModel;
import io.cockroachdb.pestcontrol.service.ResourceNotFoundException;
import io.cockroachdb.pestcontrol.util.DataPoint;
import io.cockroachdb.pestcontrol.util.Metrics;

/**
 * Manages background workers and metric data points for a single cluster.
 */
public class WorkerRegistry {
    private static final long samplePeriodSeconds = TimeUnit.MINUTES.toSeconds(5);

    private final List<WorkerModel> workers = Collections.synchronizedList(new ArrayList<>());

    private final List<DataPoint<Integer>> dataPoints = Collections.synchronizedList(new ArrayList<>());

    public void addWorker(WorkerModel worker) {
        workers.add(worker);
    }

    public void deleteWorker(Integer id) {
        WorkerModel workload = findWorkerById(id);
        if (workload.isRunning()) {
            throw new IllegalStateException("Workload is running: " + id);
        }

        if (workload.isRunning()) {
            throw new IllegalStateException("Workload is running: " + id);
        }
        workers.remove(workload);
    }

    public WorkerModel findWorkerById(Integer id) {
        return workers
                .stream()
                .filter(worker -> Objects.equals(worker.getId(), id))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No worker with id: " + id));
    }

    public List<WorkerModel> getWorkers() {
        return Collections.unmodifiableList(workers);
    }

    public void cancelWorkers() {
        workers.stream()
                .filter(WorkerModel::isRunning)
                .forEach(WorkerModel::cancel);
    }

    public void clearWorkers() {
        cancelWorkers();
        workers.clear();
    }

    public List<Instant> getTimeSeriesInterval() {
        return dataPoints.stream().map(DataPoint::getInstant).toList();
    }

    public List<Metrics> getTimeSeriesValues(Integer id) {
        List<Metrics> latencies = new ArrayList<>();
        dataPoints.forEach(dataPoint -> latencies.add(dataPoint.get(id)));
        return latencies;
    }

    public Metrics getTimeSeriesAggregate() {
        List<Metrics> metrics = workers.stream()
                .map(WorkerModel::getMetrics).toList();
        return Metrics.builder()
                .withTime(Instant.now())
                .withMeanTimeMillis(metrics.stream()
                        .mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withOps(metrics.stream().mapToDouble(Metrics::getOpsPerSec).sum(),
                        metrics.stream().mapToDouble(Metrics::getOpsPerMin).sum())
                .withP50(metrics.stream().mapToDouble(Metrics::getP50).average().orElse(0))
                .withP90(metrics.stream().mapToDouble(Metrics::getP90).average().orElse(0))
                .withP95(metrics.stream().mapToDouble(Metrics::getP95).average().orElse(0))
                .withP99(metrics.stream().mapToDouble(Metrics::getP99).average().orElse(0))
                .withP999(metrics.stream().mapToDouble(Metrics::getP999).average().orElse(0))
                .withMeanTimeMillis(metrics.stream().mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withSuccessful(metrics.stream().mapToInt(Metrics::getSuccess).sum())
                .withFails(metrics.stream().mapToInt(Metrics::getTransientFail).sum(),
                        metrics.stream().mapToInt(Metrics::getNonTransientFail).sum())
                .build();
    }

    public void updateDataPoints() {
        // Purge old data points older than sample period
        dataPoints.removeIf(item -> item.getInstant()
                .isBefore(Instant.now().minusSeconds(samplePeriodSeconds)));

        // Add new datapoint by sampling all workload metrics
        DataPoint<Integer> dataPoint = new DataPoint<>(Instant.now());
        workers.forEach(workload ->
                dataPoint.put(workload.getId(), workload.getMetrics()));

        dataPoints.add(dataPoint);
    }
}
