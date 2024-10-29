package io.cockroachdb.pestcontrol.service.workload.profile;

import javax.sql.DataSource;

public class FullScan extends AbstractWorker<Void> {
    public FullScan(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Void call() throws Exception {
        profileRepository.findByRandomId();
        return null;
    }
}
