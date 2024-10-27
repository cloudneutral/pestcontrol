package io.cockroachdb.pestcontrol.workload.profile;

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
import io.cockroachdb.pestcontrol.domain.ProfileEntity;
import io.cockroachdb.pestcontrol.repository.ProfileRepository;
import io.cockroachdb.pestcontrol.repository.JdbcProfileRepository;
import io.cockroachdb.pestcontrol.domain.WorkloadType;
import io.cockroachdb.pestcontrol.service.workload.ProfileWorkloads;

public class ProfileWorkloadsTest extends AbstractIntegrationTest {
    private ProfileWorkloads profileWorkloads;

    private ProfileRepository profileRepository;

    @BeforeAll
    public void setupTestOnce() {
        DataSource dataSource = applicationModel.getDataSource("integration-test");

        logger.info("Connected to: %s".formatted(
                new JdbcTemplate(dataSource)
                        .queryForObject("select version()", String.class)));

        this.profileWorkloads = new ProfileWorkloads(dataSource);

        this.profileRepository = new JdbcProfileRepository(dataSource);
        this.profileRepository.deleteAll();
    }

    @Order(0)
    @Test
    public void whenStartingInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_insert);
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

        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_batch_insert);
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
        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_update);
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
        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_delete);
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
        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_read);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });

        Callable<?> action2 = profileWorkloads.createWorkloadAction(WorkloadType.profile_follower_read);
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
        Callable<?> action = profileWorkloads.createWorkloadAction(WorkloadType.profile_scan);
        IntStream.rangeClosed(1, 10).forEach(value -> {
            try {
                action.call();
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }
}
