package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class UpdateOne extends CyclicWorker<Void> {
    public UpdateOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            findNextProfile(false)
                    .ifPresent(profileRepository::updateProfile);
        });
        return null;
    }
}
