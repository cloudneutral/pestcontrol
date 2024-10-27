package io.cockroachdb.pestcontrol.service;

import java.util.List;

import io.cockroachdb.pestcontrol.model.NodeModel;
import io.cockroachdb.pestcontrol.model.nodes.NodeDetail;
import io.cockroachdb.pestcontrol.model.status.NodeStatus;

public interface ClusterManager {
    List<String> getClusterIds();

    String getClusterVersion(String clusterId);

    void setCredentialsHandler(CredentialsHandler credentialsHandler);

    String login(String clusterId, String userName, String password);

    boolean logout(String clusterId);

    boolean hasSessionToken(String clusterId);

    NodeDetail queryNodeDetailById(String clusterId, Integer id);

    NodeStatus queryNodeStatusById(String clusterId, Integer id);

    NodeModel queryNodeById(String clusterId, Integer id);

    List<NodeModel> queryAllNodes(String clusterId);

    void disruptNode(String clusterId, Integer id);

    void disruptRegion(String clusterId, String regionName);

    void recoverNode(String clusterId, Integer id);

    void recoverRegion(String clusterId, String regionName);
}
