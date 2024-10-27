package io.cockroachdb.pestcontrol.service.workload;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.cockroachdb.pestcontrol.domain.ProfileEntity;
import io.cockroachdb.pestcontrol.domain.WorkloadType;
import io.cockroachdb.pestcontrol.repository.JdbcProfileRepository;
import io.cockroachdb.pestcontrol.repository.ProfileRepository;

/**
 * Customer profile workloads.
 */
public class ProfileWorkloads {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<Optional<ProfileEntity>> latestEntity
            = new AtomicReference<>(Optional.empty());

    private final PlatformTransactionManager platformTransactionManager;

    private final ProfileRepository profileRepository;

    private final JdbcTemplate jdbcTemplate;

    public ProfileWorkloads(DataSource dataSource) {
        this.platformTransactionManager = new DataSourceTransactionManager(dataSource);
        this.profileRepository = new JdbcProfileRepository(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        initSchema(dataSource);
    }

    private void initSchema(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setCommentPrefix("--");
        populator.setIgnoreFailedDrops(true);
        populator.addScript(new ClassPathResource("db/profile/profile-create.sql"));

        try {
            DatabasePopulatorUtils.execute(populator, dataSource);
        } catch (DataAccessException e) {
            logger.warn("Unable to create db schema - continuing");
        }
    }

    public Callable<?> createWorkloadAction(WorkloadType workloadType) {
        return switch (workloadType) {
            case profile_insert -> createInsertAction();
            case profile_batch_insert -> createBatchInsertAction();
            case profile_update -> createUpdateAction();
            case profile_delete -> createDeleteAction();
            case profile_read -> createReadAction(false);
            case profile_follower_read -> createReadAction(true);
            case profile_scan -> createScanAction();
            case select_one -> createSelectOneAction();
            case random_wait -> createRandomSleepAction();
            case fixed_wait -> createFixedSleepAction();
        };
    }

    private Optional<ProfileEntity> findNextProfile(boolean followerRead) {
        Optional<ProfileEntity> e = latestEntity.get();
        if (e.isPresent()) {
            e = profileRepository.findByNextId(e.get().getId(), followerRead);
        }
        if (e.isEmpty()) {
            e = profileRepository.findFirst(followerRead);
        }
        latestEntity.set(e);
        return e;
    }

    private Callable<?> createRandomSleepAction() {
        return () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if (random.nextDouble(1.0) > 0.95) {
                TimeUnit.MILLISECONDS.sleep(random.nextLong(500, 2500));
            } else {
                TimeUnit.MILLISECONDS.sleep(random.nextLong(0, 5));
            }
            return 0;
        };
    }

    private Callable<?> createFixedSleepAction() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(500);
            return 0;
        };
    }

    private Callable<?> createInsertAction() {
        return () -> {
            profileRepository.insertProfileSingleton();
            return 0;
        };
    }

    private Callable<?> createBatchInsertAction() {
        return () -> {
            profileRepository.insertProfileBatch(32);
            return 0;
        };
    }

    private Callable<?> createUpdateAction() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        return () -> {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                findNextProfile(false).ifPresent(profileRepository::updateProfile);
            });
            return 0;
        };
    }

    private Callable<?> createDeleteAction() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        return () -> {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                findNextProfile(false).ifPresent(profileEntity ->
                        profileRepository.deleteProfileById(profileEntity.getId()));
            });
            return 0;
        };
    }

    private Callable<?> createReadAction(boolean followerRead) {
        return () -> {
            findNextProfile(followerRead);
            return 0;
        };
    }

    private Callable<?> createScanAction() {
        return () -> {
            profileRepository.findByRandomId();
            return 0;
        };
    }

    private Callable<?> createSelectOneAction() {
        return () -> {
            jdbcTemplate.execute("select 1");
            return 0;
        };
    }
}
