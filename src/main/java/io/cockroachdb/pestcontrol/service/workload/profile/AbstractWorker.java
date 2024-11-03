package io.cockroachdb.pestcontrol.service.workload.profile;

import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

import io.cockroachdb.pestcontrol.repository.JdbcProfileRepository;
import io.cockroachdb.pestcontrol.repository.ProfileRepository;

public abstract class AbstractWorker<T> implements Callable<T> {
    protected final ProfileRepository profileRepository;

    protected final JdbcTemplate jdbcTemplate;

    protected final TransactionTemplate transactionTemplate;

    protected AbstractWorker(DataSource dataSource) {
        this.profileRepository = new JdbcProfileRepository(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

        initSchema(dataSource);
    }

    private void initSchema(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setCommentPrefix("--");
        populator.setIgnoreFailedDrops(true);
        populator.addScript(new ClassPathResource("db/profile/profile-create.sql"));

        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
