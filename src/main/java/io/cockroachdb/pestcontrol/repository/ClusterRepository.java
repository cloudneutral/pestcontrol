package io.cockroachdb.pestcontrol.repository;

public interface ClusterRepository {
    String queryNodeStatus();

    String queryNodeStatusById(Integer id);
}
