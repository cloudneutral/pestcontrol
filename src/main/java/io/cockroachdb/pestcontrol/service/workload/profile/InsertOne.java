package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class InsertOne extends CyclicWorker<Void> {
    public InsertOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        profileRepository.insertProfileSingleton();
        return null;
    }
}
