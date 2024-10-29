package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class DeleteOne extends CyclicWorker<Void> {
    public DeleteOne(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            findNextProfile(false).ifPresent(profileEntity ->
                    profileRepository.deleteProfileById(profileEntity.getId()));
        });
        return null;
    }
}
