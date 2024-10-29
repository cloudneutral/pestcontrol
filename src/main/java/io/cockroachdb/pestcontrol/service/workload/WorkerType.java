package io.cockroachdb.pestcontrol.service.workload;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import io.cockroachdb.pestcontrol.service.workload.profile.DeleteOne;
import io.cockroachdb.pestcontrol.service.workload.profile.FullScan;
import io.cockroachdb.pestcontrol.service.workload.profile.InsertBatch;
import io.cockroachdb.pestcontrol.service.workload.profile.InsertOne;
import io.cockroachdb.pestcontrol.service.workload.profile.ReadOne;
import io.cockroachdb.pestcontrol.service.workload.profile.SelectOne;
import io.cockroachdb.pestcontrol.service.workload.profile.UpdateOne;

public enum WorkerType {
    profile_insert("Profile singleton insert",
            "A single insert statement.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new InsertOne(dataSource);
        }
    },
    profile_batch_insert("Profile batch insert",
            "A single batch of 32 insert statements.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new InsertBatch(dataSource);
        }
    },
    profile_update("Profile point read and update",
            "A single point lookup read followed by an update.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new UpdateOne(dataSource);
        }
    },
    profile_delete("Profile point read and delete",
            "A single point lookup read followed by a delete.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new DeleteOne(dataSource);
        }
    },
    profile_read("Profile point read",
            "A single authoritative point lookup read.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new ReadOne(dataSource, false);
        }
    },
    profile_follower_read("Profile follower read",
            "A single historical point lookup read.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new ReadOne(dataSource, true);
        }
    },
    profile_scan("Profile full scan",
            "A single full table scan.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new FullScan(dataSource);
        }
    },
    select_one("Select one",
            "A basic 'select 1' statement.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return new SelectOne(dataSource);
        }
    },
    random_wait("Random wait",
            "A random wait not touching the DB.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return () -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (random.nextDouble(1.0) > 0.95) {
                    TimeUnit.MILLISECONDS.sleep(random.nextLong(500, 2500));
                } else {
                    TimeUnit.MILLISECONDS.sleep(random.nextLong(0, 5));
                }
                return null;
            };
        }
    },
    fixed_wait("Fixed wait",
            "A fixed wait not touching the DB.") {
        @Override
        public Worker<?> createWorker(DataSource dataSource) {
            return () -> {
                TimeUnit.MILLISECONDS.sleep(500);
                return null;
            };
        }
    };

    private final String displayValue;

    private final String description;

    WorkerType(String displayValue, String description) {
        this.displayValue = displayValue;
        this.description = description;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getDescription() {
        return description;
    }

    public abstract Worker<?> createWorker(DataSource dataSource);
}
