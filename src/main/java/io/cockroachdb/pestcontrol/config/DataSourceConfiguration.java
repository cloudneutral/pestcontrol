package io.cockroachdb.pestcontrol.config;

import java.sql.Connection;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
public class DataSourceConfiguration {
    public static final String SQL_TRACE_LOGGER = "io.cockroachdb.pestcontrol.SQL_TRACE";

    private final Logger logger = LoggerFactory.getLogger(SQL_TRACE_LOGGER);

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Function<DataSourceProperties, ClosableDataSource> dataSourceFactory() {
        return props -> {
            HikariDataSource ds = props
                    .initializeDataSourceBuilder()
                    .type(HikariDataSource.class)
                    .build();

            HikariConfig hikariConfig = hikariConfig();

            ds.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
            ds.setMinimumIdle(hikariConfig.getMinimumIdle());
            ds.setInitializationFailTimeout(hikariConfig.getInitializationFailTimeout());
            ds.setConnectionTimeout(hikariConfig.getConnectionTimeout());
            ds.setValidationTimeout(hikariConfig.getValidationTimeout());
            ds.setIdleTimeout(hikariConfig.getIdleTimeout());
            ds.setMaxLifetime(hikariConfig.getMaxLifetime());
            ds.setKeepaliveTime(hikariConfig.getKeepaliveTime());

            ds.setPoolName(props.getName());
            ds.setAutoCommit(true);
            ds.addDataSourceProperty("reWriteBatchedInserts", "true");
            ds.addDataSourceProperty("application_name", "Pest Control");

            return new ClosableDataSource(loggingProxy(ds));
        };
    }

    private DataSource loggingProxy(DataSource dataSource) {
        if (!logger.isTraceEnabled()) {
            return dataSource;
        }

        DefaultQueryLogEntryCreator creator = new DefaultQueryLogEntryCreator();
        creator.setMultiline(true);

        SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
        listener.setLogger(logger);
        listener.setLogLevel(SLF4JLogLevel.TRACE);
        listener.setQueryLogEntryCreator(creator);
        listener.setWriteConnectionId(true);

        return ProxyDataSourceBuilder
                .create(dataSource)
                .name("SQL-Trace")
                .listener(listener)
                .asJson()
                .build();
    }

    @Deprecated // prob dont need it
    private DataSource lazyProxy(DataSource target) {
        LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy();
        proxy.setTargetDataSource(target);
        proxy.setDefaultAutoCommit(true);
        proxy.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return proxy;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}

