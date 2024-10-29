package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class InsertBatch extends AbstractWorker<Void> {
    public InsertBatch(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        profileRepository.insertProfileBatch(32);
        return null;
    }
}
