package io.cockroachdb.pestcontrol.config;

import java.io.Closeable;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.Assert;

import com.zaxxer.hikari.HikariDataSource;

public class ClosableDataSource extends DelegatingDataSource implements Closeable, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ClosableDataSource(DataSource dataSource) {
        super(dataSource);

        Assert.notNull(dataSource, "dataSource is null");
        try {
            Assert.isTrue(dataSource.isWrapperFor(HikariDataSource.class), "dataSource is not a wrapper for HikariDS");
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void destroy() {
        try {
            HikariDataSource dataSource = unwrap(HikariDataSource.class);
            logger.info("Closing (on destroy) datasource: {}", dataSource);
            dataSource.close();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        try {
            HikariDataSource dataSource = unwrap(HikariDataSource.class);
            logger.info("Closing (on close) datasource: {}", dataSource);
            dataSource.close();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
