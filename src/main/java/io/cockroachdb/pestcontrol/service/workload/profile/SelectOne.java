package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class SelectOne extends AbstractWorker<Void> {
    public SelectOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        jdbcTemplate.execute("select 1");
        return null;
    }
}
