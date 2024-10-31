package io.cockroachdb.pestcontrol.service.workload.profile;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import io.cockroachdb.pestcontrol.repository.ProfileEntity;

public abstract class CyclicWorker<T> extends AbstractWorker<T> {
    private final AtomicReference<Optional<ProfileEntity>> latestEntity
            = new AtomicReference<>(Optional.empty());

    public CyclicWorker(DataSource dataSource) {
        super(dataSource);
    }

    protected Optional<ProfileEntity> findNextProfile(boolean followerRead) {
        Optional<ProfileEntity> e = latestEntity.get();
        if (e.isPresent()) {
            e = profileRepository.findByNextId(e.get().getId(), followerRead);
        }
        if (e.isEmpty()) {
            e = profileRepository.findFirst(followerRead);
        }
        latestEntity.set(e);
        return e;
    }
}

