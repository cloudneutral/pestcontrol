package io.cockroachdb.pestcontrol.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.cockroachdb.pestcontrol.domain.ProfileEntity;

public interface ProfileRepository {
    @Deprecated
    boolean isSchemaReady();

    ProfileEntity insertProfileSingleton();

    List<ProfileEntity> insertProfileBatch(int batchSize);

    void updateProfile(ProfileEntity profile);

    @Deprecated
    void updateRandomProfile();

    void deleteProfileById(UUID id);

    @Deprecated
    void deleteRandomProfile();

    List<ProfileEntity> findAll(int limit);

    Optional<ProfileEntity> findFirst(boolean followerRead);

    Optional<ProfileEntity> findByNextId(UUID id, boolean followerRead);

    Optional<ProfileEntity> findByRandomId();

    @Deprecated
    Optional<ProfileEntity> findById(UUID id);

    void deleteAll();
}
