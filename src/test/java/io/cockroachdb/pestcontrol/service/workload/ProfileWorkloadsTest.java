package io.cockroachdb.pestcontrol.service.workload;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.cockroachdb.pestcontrol.AbstractIntegrationTest;
import io.cockroachdb.pestcontrol.repository.ProfileEntity;
import io.cockroachdb.pestcontrol.repository.ProfileRepository;
import io.cockroachdb.pestcontrol.repository.JdbcProfileRepository;

public class ProfileWorkloadsTest extends AbstractIntegrationTest {
    private ProfileRepository profileRepository;

    private DataSource dataSource;

    @BeforeAll
    public void setupTestOnce() {
        this.dataSource = applicationProperties.getDataSource("integration-test");

        logger.info("Connected to: %s".formatted(
                new JdbcTemplate(dataSource)
                        .queryForObject("select version()", String.class)));

        this.profileRepository = new JdbcProfileRepository(dataSource);
        this.profileRepository.deleteAll();
    }

    @Order(0)
    @Test
    public void whenStartingInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        Callable<?> action = WorkerType.profile_insert
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<ProfileEntity> after = profileRepository.findAll(65536);
        Assertions.assertEquals(before.size() + 10, after.size());
    }

    @Order(1)
    @Test
    public void whenStartingBatchInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        Callable<?> action = WorkerType.profile_batch_insert
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        List<ProfileEntity> after = profileRepository.findAll(65536);
        Assertions.assertEquals(before.size() + 10 * 32, after.size());
    }

    @Order(2)
    @Test
    public void whenStartingUpdateWorkload_thenExpectRowsAffected() {
        Callable<?> action = WorkerType.profile_update
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(4)
    @Test
    public void whenStartingDeleteWorkload_thenExpectRowsAffected() {
        Callable<?> action = WorkerType.profile_delete
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(5)
    @Test
    public void whenStartingReadWorkload_thenExpectRows() {
        Callable<?> action = WorkerType.profile_read
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Callable<?> action2 = WorkerType.profile_follower_read
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action2.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    @Order(5)
    @Test
    public void whenStartingScanWorkload_thenExpectRows() {
        Callable<?> action = WorkerType.profile_scan
                .createWorker(dataSource);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }
}
