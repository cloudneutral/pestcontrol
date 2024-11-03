package io.cockroachdb.pestcontrol.util;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataPoint<ID> {
    private final Instant instant;

    /**
     * Maps workload IDs to call metric snapshots.
     */
    private final Map<ID, Metrics> metrics = new LinkedHashMap<>();

    public DataPoint(Instant instant) {
        this.instant = instant;
    }

    public Instant getInstant() {
        return instant;
    }

    public void mark(ID id, Metrics from) {
        metrics.put(id, Metrics.builder()
                .withSuccessful(from.getSuccess())
                .withFails(from.getTransientFail(), from.getNonTransientFail())
                .withOps(from.getOpsPerSec(), from.getOpsPerMin())
                .withP50(from.getP50())
                .withP90(from.getP90())
                .withP95(from.getP95())
                .withP99(from.getP99())
                .withP999(from.getP999())
                .withUpdateTime(from.getUpdateTime())
                .withMeanTimeMillis(from.getMeanTimeMillis())
                .build()
        );
    }

    public Metrics get(ID id) {
        return metrics.getOrDefault(id, Metrics.empty());
    }
}
