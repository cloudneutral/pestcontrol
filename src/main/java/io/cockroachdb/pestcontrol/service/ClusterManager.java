package io.cockroachdb.pestcontrol.service;

import java.util.List;

import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.schema.nodes.NodeDetail;
import io.cockroachdb.pestcontrol.schema.status.NodeStatus;

public interface ClusterManager {
    List<String> getClusterIds();

    String getClusterVersion(String clusterId);

    ClusterType getClusterType(String clusterId);

    void setCredentialsHandler(CredentialsHandler credentialsHandler);

    String login(String clusterId, String userName, String password);

    boolean logout(String clusterId);

    boolean hasSessionToken(String clusterId);

    NodeDetail queryNodeDetailById(String clusterId, Integer id);

    NodeStatus queryNodeStatusById(String clusterId, Integer id);

    NodeModel queryNodeById(String clusterId, Integer id);

    List<NodeModel> queryAllNodes(String clusterId);

    void disruptNode(String clusterId, Integer id);

    void disruptLocality(String clusterId, String tiers);

    void recoverNode(String clusterId, Integer id);

    void recoverLocality(String clusterId, String tiers);
}
