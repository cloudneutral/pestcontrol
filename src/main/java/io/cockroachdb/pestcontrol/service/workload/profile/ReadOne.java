package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class ReadOne extends CyclicWorker<Void> {
    private final boolean followerRead;

    public ReadOne(DataSource dataSource, boolean followerRead) {
        super(dataSource);
        this.followerRead = followerRead;
    }

    @Override
    public Void call() throws Exception {
        findNextProfile(followerRead);
        return null;
    }
}
